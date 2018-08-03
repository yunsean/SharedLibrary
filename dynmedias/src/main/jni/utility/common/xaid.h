#pragma once
#include <functional>

namespace std {
	class destruct_invoke_function {
	public:
		destruct_invoke_function(const std::function<void ()>& fn) 
			: m_entry(fn){
		}
		~destruct_invoke_function() {
			if (m_entry)m_entry();
		}
	private:
		std::function<void ()> m_entry;
	};

	template <typename C, typename S>
	class member_destruct_invoke;
	template <typename T, typename R>
	class member_destruct_invoke <T, R()>{
	public:
		member_destruct_invoke(R (T::*fn)(), T* self)
			: m_entry(fn)
			, m_self(*self){
		}
		~member_destruct_invoke(){
			m_entry(m_self);
		}
	private:
		std::mem_fun_ref_t<R, T> m_entry;
		T&	m_self;
	};

	template <typename T, typename R, typename P1>
	class member_destruct_invoke <T, R (P1)>{
	public:
		member_destruct_invoke(R (T::*fn)(P1 p1), T* self, P1 param)
			: m_entry(fn)
			, m_self(*self)
			, m_param(param){
		}
		~member_destruct_invoke(){
			m_entry(m_self, m_param);
		}
	private:
		std::mem_fun1_ref_t<R, T, P1> m_entry;
		T&	m_self;
		P1 m_param;
	};

	template <class T, class R>
	inline member_destruct_invoke<T, R()>   destruct_invoke(T* c, R (T::*fn)()) {
		return member_destruct_invoke<T, R()>(fn, c);
	}
	template <class T, class R, class P1>
	inline member_destruct_invoke<T, R(P1)> destruct_invoke(T* c, R (T::*fn)(P1), P1 p) {
		return member_destruct_invoke<T, R(P1)>(fn, c, p);
	}
	inline destruct_invoke_function destruct_invoke(const std::function<void ()>& fn) {
		return destruct_invoke_function(fn);
	}
};