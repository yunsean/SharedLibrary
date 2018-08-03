#pragma once 
#include <mutex>
#ifdef WIN32
#include <synchapi.h>
#include <processthreadsapi.h>
#endif

namespace std {

	class xcritic {

	public:
		void lock() {
#ifdef WIN32 
			if (::GetCurrentThreadId() == holderTid){
				holderCount++;
				return;
			}
			::EnterCriticalSection(&critic);
			holderTid		= ::GetCurrentThreadId();
			holderCount++;
#else 
			mutex.lock();
#endif
		}
		void unlock() {
#ifdef WIN32
			if (::GetCurrentThreadId() != holderTid){
				return;
			}
			holderCount--;
			if (holderCount == 0) {
				holderTid	= 0;
				::LeaveCriticalSection(&critic);
			}
#else 
			mutex.unlock();
#endif
		}
		bool trylock() {
#ifdef WIN32
			if (::GetCurrentThreadId() == holderTid){
				holderCount++;
				return true;
			}
			bool			res(::TryEnterCriticalSection(&critic) ? true : false);
			if (!res)return false;
			::EnterCriticalSection(&critic);
			holderTid		= ::GetCurrentThreadId();
			holderCount++;
			return true;
#else 
			return mutex.try_lock();
#endif
		}

	public:
		xcritic() 
#ifdef WIN32
			: holderTid(0)
			, holderCount(0) {
				::InitializeCriticalSection(&critic);
#else 
			: mutex(){ 
#endif
		}
		~xcritic() {
#ifdef WIN32
			::DeleteCriticalSection(&critic);
#endif
		}

    private:
#ifdef WIN32
		CRITICAL_SECTION		critic;
		unsigned int			holderTid;
		unsigned int			holderCount;
#else 
		std::recursive_mutex	mutex;
#endif
	};
};