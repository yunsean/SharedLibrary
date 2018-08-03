#pragma once
#include <thread>
#include <functional>
#include "xexception.h"

namespace std {

    class xthread {
    public:
        xthread() 
            : thread(nullptr) {
        }
        template <typename FN>
        xthread(FN fn)
            : thread(new std::thread(fn)) {
        }
        template <typename FN, typename P1>
        xthread(FN fn, P1 p)
            : thread(new std::thread(fn, p)){
        }
    public:
        template <typename FN>
        void run(FN fn) {
            if (thread != nullptr) {
                throw std::xexception("thread is working!");
            }
            thread = new std::thread(fn);
        }
        template <typename FN, typename P1>
        void run(FN fn, P1 p) {
            if (thread != nullptr) {
                throw std::xexception("thread is working!");
            }
            thread = new std::thread(fn, p);
        }
        void attach(std::thread* t) {
            if (thread != nullptr) {
                throw std::xexception("thread is working!");
            }
            thread = t;
        }
        bool joinable() {
            if (thread == nullptr)return false;
            return thread->joinable();
        }
        void join() {
            if (thread == nullptr)return;
            thread->join();
        }
		void close() {
			if (thread == nullptr)return;
			if (thread->joinable()) {
				thread->join();
			}
			thread = nullptr;
		}
#ifdef WIN32
        void* native_handle() {
            if (thread == nullptr)return nullptr;
#else 
        int native_handle() {
            if (thread == nullptr)return 0;
#endif
            return thread->native_handle();
        }

    public:
        ~xthread() {
            if (thread != nullptr) {
                if (thread->joinable())thread->join();
                delete thread;
            }
            thread = nullptr;
        }
    private:
        xthread(const xthread&);
        xthread& operator=(const xthread&);
    private:
        std::thread*     thread;
    };
}