package com.dylan.medias.codec;

public class MxAvcConfig {

	@SuppressWarnings("unused")
	public boolean parse(byte[] nal) {
		if (nal == null) return false;
		if (nal.length < 4)return false;
		if (nal[0] != 0x00 || nal[1] != 0x00)return false;
		byte[] sps = null;
		if (nal[2] == 0x01) {
			sps = new byte[nal.length - 3];
			System.arraycopy(nal, 3, sps, 0, nal.length - 3);
		} else if (nal[2] == 0x00 && nal[3] == 0x01) {
			sps = new byte[nal.length - 4];
			System.arraycopy(nal, 4, sps, 0, nal.length - 4);			
		} else {
			return false;
		}
		mStartBit = 0;
		int forbidden_zero_bit = u(1, sps); 
		int nal_ref_idc = u(2, sps);
		int nal_unit_type = u(5, sps);
		if (nal_unit_type == 7) {
			int profile_idc = u(8, sps);
			int constraint_set0_flag = u(1, sps);
			int constraint_set1_flag = u(1, sps);
			int constraint_set2_flag = u(1, sps);
			int constraint_set3_flag = u(1, sps);
			int reserved_zero_4bits = u(4, sps);	
			int level_idc = u(8, sps);
			int seq_parameter_set_id = ue(sps);
			if( profile_idc == 100 || profile_idc == 110 || profile_idc == 122 || profile_idc == 144 ) {
				int chroma_format_idc = ue(sps);
				if (chroma_format_idc == 3) {
					int residual_colour_transform_flag = u(1, sps);
				}
				int bit_depth_luma_minus8 = ue(sps);
				int bit_depth_chroma_minus8 = ue(sps);
				int qpprime_y_zero_transform_bypass_flag = u(1, sps);
				int seq_scaling_matrix_present_flag = u(1, sps);
			}
			int log2_max_frame_num_minus4 = ue(sps);
			int pic_order_cnt_type = ue(sps);
			if (pic_order_cnt_type == 0) {
				int log2_max_pic_order_cnt_lsb_minus4 = ue(sps);
			} else if (pic_order_cnt_type == 1) {
				int delta_pic_order_always_zero_flag = u(1, sps);
				int offset_for_non_ref_pic = se(sps);
				int offset_for_top_to_bottom_field = se(sps);
				int num_ref_frames_in_pic_order_cnt_cycle = ue(sps);
				if (num_ref_frames_in_pic_order_cnt_cycle > 256) {
					num_ref_frames_in_pic_order_cnt_cycle = 256;
				}
				while (num_ref_frames_in_pic_order_cnt_cycle > 0) {
					int i_offset_for_ref_frame = se(sps);
					num_ref_frames_in_pic_order_cnt_cycle--;
				}
			}
			int num_ref_frames = ue(sps);
			int gaps_in_frame_num_value_allowed_flag = u(1, sps);
			int pic_width_in_mbs_minus1 = ue(sps);
			int pic_height_in_map_units_minus1 = ue(sps);
			mWidth = (pic_width_in_mbs_minus1 + 1) * 16;
			mHeight =(pic_height_in_map_units_minus1 + 1) * 16;
			return true;
		} else {
			return false;
		}
	}
	public int getWidth() {
		return mWidth;
	}
	public int getHeight() {
		return mHeight;
	}
	
	private int mStartBit = 0;
	private int mWidth = 0;
	private int mHeight = 0;
	private int ue(byte[] data) {
	    int nZeroNum = 0;
	    while (mStartBit < data.length * 8) {
	        if ((data[mStartBit / 8] & (0x80 >> (mStartBit % 8))) != 0) {
	            break;
	        }
	        nZeroNum++;
	        mStartBit++;
	    }
	    mStartBit++;
	    int dwRet = 0;
	    for (int i = 0; i < nZeroNum; i++) {
	        dwRet <<= 1;
	        if ((data[mStartBit / 8] & (0x80 >> (mStartBit % 8))) != 0) {
	            dwRet++;
	        }
	        mStartBit++;
	    }
	    return (1 << nZeroNum) - 1 + dwRet;
	}
	int se(byte[] data) {
		int UeVal = ue(data);
		double k = UeVal;
		int nValue = (int) Math.ceil(k/2);
		if (UeVal % 2 == 0) {
			nValue = -nValue;
		}
		return nValue;
	}
	int u(int BitCount, byte[] sps) {
	    int dwRet = 0;
	    for (int i = 0; i < BitCount; i++) {
	        dwRet <<= 1;
	        if ((sps[mStartBit / 8] & (0x80 >> (mStartBit % 8))) != 0) {
	            dwRet += 1;
	        }
	        mStartBit++;
	    }
	    return dwRet;
	}
}
