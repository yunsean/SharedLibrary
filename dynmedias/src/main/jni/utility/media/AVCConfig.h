#pragma once
#include <vector>
#include "SmartNal.h"

class CAVCConfig
{
public:
	CAVCConfig(void);
	virtual ~CAVCConfig(void);

public:
	bool			Serialize(CSmartNal<unsigned char>& config);
	void			Unserialize(const unsigned char* const config, const int size);

	void			AddSps(const unsigned char* const sps, const int size);
	int				SpsCount();
	bool			GetSps(const int index, CSmartNal<unsigned char>& sps);

	void			AddPps(const unsigned char* const pps, const int size);
	int				PpsCount();
	bool			GetPps(const int index, CSmartNal<unsigned char>& pps);

public:
	int				configurationVersion;
	int				profileIndication;
	int				profile_compatibility;
	int				levelIndication;
	int				lengthSizeMinusOne;

protected:
	int				CalcBufSize();

private:
	std::vector<CSmartNal<unsigned char>>*	m_vsnSPS;
	std::vector<CSmartNal<unsigned char>>*	m_vsnPPS;
};

