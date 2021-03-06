/*
    Copyright (C) 2008 Red Hat Software - JBoss Middleware Division


    This library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
    Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public
    License along with this library; if not, write to the Free Software
    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
    USA

    The GNU Lesser General Public License is available in the file COPYING.
    
    Software written by Clebert Suconic (csuconic at redhat dot com)
*/

#ifndef _GNU_SOURCE
#define _GNU_SOURCE
#endif


#include <stdlib.h>
#include <list>
#include <iostream>
#include <sstream>
#include <memory.h>
#include <errno.h>
#include <libaio.h>
#include <fcntl.h>
#include "AsyncFile.h"
#include "AIOController.h"
#include "AIOException.h"
#include "pthread.h"
#include "LockClass.h"
#include "CallbackAdapter.h"
#include "LockClass.h"

//#define DEBUG

#define WAIT_FOR_SPOT 10000
#define TRIES_BEFORE_WARN 0
#define TRIES_BEFORE_ERROR 500


std::string io_error(int rc)
{
	std::stringstream buffer;
	
	if (rc == -ENOSYS)
		buffer << "AIO not in this kernel";
	else 
		buffer << "Error:= " << strerror(-rc);
	
	return buffer.str();
}


AsyncFile::AsyncFile(std::string & _fileName, AIOController * _controller, int _maxIO) : aioContext(0), events(0), fileHandle(0), controller(_controller), pollerRunning(0)
{
	::pthread_mutex_init(&fileMutex,0);
	::pthread_mutex_init(&pollerMutex,0);
	
	maxIO = _maxIO;
	fileName = _fileName;
	if (io_queue_init(maxIO, &aioContext))
	{
		throw AIOException(1, "Can't initialize aio"); 
	}

	fileHandle = ::open(fileName.data(),  O_RDWR | O_CREAT | O_DIRECT, 0666);
	if (fileHandle < 0)
	{
		throw AIOException(1, "Can't open file"); 
	}
	
#ifdef DEBUG
	fprintf (stderr,"File Handle %d", fileHandle);
#endif

	events = (struct io_event *)malloc (maxIO * sizeof (struct io_event));
	
	if (events == 0)
	{
		throw AIOException (1, "Can't allocate ioEvents");
	}

}

AsyncFile::~AsyncFile()
{
	if (io_queue_release(aioContext))
	{
		throw AIOException(2,"Can't release aio");
	}
	if (::close(fileHandle))
	{
		throw AIOException(2,"Can't close file");
	}
	free(events);
	::pthread_mutex_destroy(&fileMutex);
	::pthread_mutex_destroy(&pollerMutex);
}

int isException (THREAD_CONTEXT threadContext)
{
	return JNI_ENV(threadContext)->ExceptionOccurred() != 0;
}

void AsyncFile::pollEvents(THREAD_CONTEXT threadContext)
{
	
	LockClass lock(&pollerMutex);
	pollerRunning=1;
	
	// TODO: Maybe we don't need to wait for one second.... we just keep waiting forever, and use something to interrupt it
	// maybe an invalid write to interrupt it.
	struct timespec oneSecond;
	oneSecond.tv_sec = 1;
	oneSecond.tv_nsec = 0;
	
	
	while (pollerRunning)
	{
		if (isException(threadContext))
		{
			return;
		}
		int result = io_getevents(this->aioContext, 1, maxIO, events, 0);
		
		
#ifdef DEBUG
		fprintf (stderr, "poll, pollerRunning=%d\n", pollerRunning); fflush(stderr);
#endif
		
		if (result > 0)
		{
			
#ifdef DEBUG
			fprintf (stdout, "Received %d events\n", result);
			fflush(stdout);
#endif
		}

		for (int i=0; i<result; i++)
		{
			
			struct iocb * iocbp = events[i].obj;
	
			if (iocbp->data == (void *) -1)
			{
				pollerRunning = 0;
//				controller->log(threadContext, 2, "Received poller request to stop");
			}
			else
			{
				CallbackAdapter * adapter = (CallbackAdapter *) iocbp->data;
				
				long result = events[i].res;
				if (result < 0)
				{
					std::string strerror = io_error(result);
					adapter->onError(threadContext, result, strerror);
				}
				else
				{
					adapter->done(threadContext);
				}
			}
			
			delete iocbp;
		}
	}
	
//	controller->log(threadContext, 2, "Poller finished execution");
	
}


void AsyncFile::preAllocate(THREAD_CONTEXT , off_t position, int blocks, size_t size, int fillChar)
{

	if (size % ALIGNMENT != 0)
	{
		throw AIOException (101, "You can only pre allocate files in multiples of 512");
	}
	
	void * preAllocBuffer = 0;
	if (posix_memalign(&preAllocBuffer, 512, size))
	{
		throw AIOException(10, "Error on posix_memalign");
	}
	
	memset(preAllocBuffer, fillChar, size);
	
	
	if (::lseek (fileHandle, position, SEEK_SET) < 0) throw AIOException (11, "Error positioning the file");
	
	for (int i=0; i<blocks; i++)
	{
		if (::write(fileHandle, preAllocBuffer, size)<0)
		{
			throw AIOException (12, "Error pre allocating the file");
		}
	}
	
	if (::lseek (fileHandle, position, SEEK_SET) < 0) throw AIOException (11, "Error positioning the file");
	
	free (preAllocBuffer);
}

void AsyncFile::write(THREAD_CONTEXT threadContext, long position, size_t size, void *& buffer, CallbackAdapter *& adapter)
{

	struct iocb * iocb = new struct iocb();
	::io_prep_pwrite(iocb, fileHandle, buffer, size, position);
	iocb->data = (void *) adapter;

	int tries = 0;
	int result = 0;
	
	while ((result = ::io_submit(aioContext, 1, &iocb)) == (-EAGAIN))
	{
#ifdef DEBUG
		fprintf (stderr, "Retrying block as iocb was full (retry=%d)\n", tries);
#endif
		tries ++;
		if (tries > TRIES_BEFORE_WARN)
		{
#ifdef DEBUG
		    fprintf (stderr, "Warning level on retries, informing logger (retry=%d)\n", tries);
#endif
			controller->log(threadContext, 1, "You should consider expanding AIOLimit if this message appears too many times");
		}
		
		if (tries > TRIES_BEFORE_ERROR)
		{
#ifdef DEBUG
		    fprintf (stderr, "Error level on retries, throwing exception (retry=%d)\n", tries);
#endif
			throw AIOException(500, "Too many retries (500) waiting for a valid iocb block, please increase MAX_IO limit");
		}
		::usleep(WAIT_FOR_SPOT);
	}
	
	if (result<0)
	{
		std::stringstream str;
		str<< "Problem on submit block, errorCode=" << result;
		throw AIOException (6, str.str());
	}
}

void AsyncFile::read(THREAD_CONTEXT threadContext, long position, size_t size, void *& buffer, CallbackAdapter *& adapter)
{

	struct iocb * iocb = new struct iocb();
	::io_prep_pread(iocb, fileHandle, buffer, size, position);
	iocb->data = (void *) adapter;

	int tries = 0;
	int result = 0;
	
	// I will hold the lock until I'm done here
	//LockClass lock(&fileMutex);
	while ((result = ::io_submit(aioContext, 1, &iocb)) == (-EAGAIN))
	{
#ifdef DEBUG
		fprintf (stderr, "Retrying block as iocb was full (retry=%d)\n", tries);
#endif
		tries ++;
		if (tries > TRIES_BEFORE_WARN)
		{
#ifdef DEBUG
		    fprintf (stderr, "Warning level on retries, informing logger (retry=%d)\n", tries);
#endif
			controller->log(threadContext, 1, "You should consider expanding AIOLimit if this message appears too many times");
		}
		
		if (tries > TRIES_BEFORE_ERROR)
		{
#ifdef DEBUG
		    fprintf (stderr, "Error level on retries, throwing exception (retry=%d)\n", tries);
#endif
			throw AIOException(500, "Too many retries (500) waiting for a valid iocb block, please increase MAX_IO limit");
		}
		::usleep(WAIT_FOR_SPOT);
	}
	
	if (result<0)
	{
		std::stringstream str;
		str<< "Problem on submit block, errorCode=" << result;
		throw AIOException (6, str.str());
	}
}

long AsyncFile::getSize()
{
	struct stat64 statBuffer;
	
	if (fstat64(fileHandle, &statBuffer) < 0)
	{
		return -1l;
	}
	return statBuffer.st_size;
}


void AsyncFile::stopPoller(THREAD_CONTEXT threadContext)
{
	pollerRunning = 0;
	
	
	struct iocb * iocb = new struct iocb();
	::io_prep_pwrite(iocb, fileHandle, 0, 0, 0);
	iocb->data = (void *) -1;

	int result = 0;
	
	while ((result = ::io_submit(aioContext, 1, &iocb)) == (-EAGAIN))
	{
		fprintf(stderr, "Couldn't send request to stop poller, trying again");
		controller->log(threadContext, 1, "Couldn't send request to stop poller, trying again");
		::usleep(WAIT_FOR_SPOT);
	}
	
//	controller->log(threadContext, 2,"Sent data to stop");
	// It will wait the Poller to gives up its lock
	LockClass lock(&pollerMutex);
}

