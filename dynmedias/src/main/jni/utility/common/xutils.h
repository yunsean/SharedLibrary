#pragma once

namespace std {
    template<bool>
    struct StaticAssert;

    template<> 
    struct StaticAssert<true>
    {};
};
#define STATIC_ASSERT(exp) {std::StaticAssert<((exp) != 0)> Failed;}

namespace std {
    struct Nil;

    template <typename T>
    struct IsPointer {
        enum { Result = false };
        typedef Nil ValueType;
    };

    template <typename T>
    struct IsPointer<T*> {
        enum { Result = true };
        typedef T ValueType;
    };
};

namespace std {

    template <class TMap, class TPred> 
    inline void map_remove_if(TMap& container, TPred pred)  { 
        for (TMap::iterator it = container.begin(); it != container.end();) {
            if (pred(*it)) {
                it = container.erase(it);
            } else {
                ++it;
            }
        }
    } 

    template <class TMap, class TKey> 
    inline void map_remove(TMap& container, TKey key)  { 
        for (TMap::iterator it = container.begin(); it != container.end();) {
            if (it->first == key) {
                it = container.erase(it);
            } else {
                ++it;
            }
        }
    }
}