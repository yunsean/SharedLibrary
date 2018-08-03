#pragma once
#include "xstring.h"
#include "SmartArr.h"

class CByte {
public:
	typedef unsigned char		BYTE;
	typedef BYTE*				LPBYTE;

public:
	CByte(int increment = 1024);
	CByte(std::nothrow_t nothrow, int increment = 1024);
	CByte(const char* const arr, std::nothrow_t nothrow = std::nothrow, int increment = 1024) throw();

public:
	CByte&			operator=(char arr[]) throw();
	CByte&			operator=(const CByte& src) throw();
	BYTE&			operator[](const int pos) throw();
	operator		BYTE*() const {return saCache;}
	operator		int() const {return szData;}
	bool			operator<(const BYTE* const right);
	bool			operator>(const BYTE* const right);
	bool			operator==(const BYTE* const right);
	bool			operator!=(const BYTE* const right);
	bool			operator==(const int nNull);
	bool			operator!=(const int nNull);
	void			Attach(BYTE* arr, const int dataSize, const int cacheSize = -1) throw();
	BYTE*			Detach();

public:
	BYTE*			EnsureSize(const int nSize, const bool bRemain = false) throw();
	int				SetSize(const int nSize) throw();
    int             FillData(const CByte& src) throw();
	int				FillData(const void* const lpData, const int nSize) throw();
    int				AppendData(const CByte& src) throw();
    int				AppendData(const void* const lpData, const int nSize) throw();
	int				FillDatas(const void* const* const lpDataPointers, const int* const nDataSizes, const int nDataCount) throw();
	int				AppendDatas(const void* const* const lpDataPointers, const int* const nDataSizes, const int nDataCount) throw();
	int				GrowSize(const int nSize) throw();
    int				GetSize() const{return szData;}
    const void*     GetData() const{return saCache.GetArr();}
    void*			GetData(){return saCache.GetArr();}
	int				GetData(void* const lpCache, const int nMaxSize);
	void			Clear();

public:
	static std::xtstring	toHex(const BYTE* const lpData, const int nSize, const int insertBlankPerNumber);
	static int				fromHex(BYTE* const lpData, const std::xtstring& strHex);
	static std::xtstring	toBase64(const BYTE* const lpData, const int nSize);
	static int				fromBase64(BYTE* const lpData, const std::xtstring& strBase64);
	static std::xtstring	toPrintable(const BYTE* const lpData, const int nSize);

public:
	std::xtstring			toHex(const int onlyTopNumber = -1, const int insertBlankPerNumber = 0);
	bool					fromHex(const std::xtstring& strHex);
	std::xtstring			toBase64(const int onlyTopNumber = -1);
	bool					fromBase64(const std::xtstring& strBase64);
	std::xtstring			toPrintable(const int onlyTopNumber = -1);

protected:
#ifdef WIN32
#pragma warning(push)
#pragma warning(disable:4251)
#endif
	bool			bNoThrow;
	int				nInc;
	CSmartArr<BYTE>	saCache;
	int				szCache;
	int				szData;
#ifdef WIN32
#pragma warning(pop)
#endif
};