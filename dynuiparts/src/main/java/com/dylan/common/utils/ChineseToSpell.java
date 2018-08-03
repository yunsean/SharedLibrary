package com.dylan.common.utils;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

public class ChineseToSpell {
	
	net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat mOutputFormat = new HanyuPinyinOutputFormat();
	public ChineseToSpell() {
		mOutputFormat.setCaseType(HanyuPinyinCaseType.UPPERCASE);
		mOutputFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
		mOutputFormat.setVCharType(HanyuPinyinVCharType.WITH_V);
	}
	
	private String mSpell = "";
	private String mCapital = "";
	public boolean convert(String hanzhis) {
		CharSequence s = hanzhis;
        char [] hanzhi = new char[s.length()];
        for(int i = 0; i < s.length(); i++){
            hanzhi[i] = s.charAt(i);
        }
        
        char [] t1 = hanzhi; 
        String[] t2 = new String[s.length()];
        
        int t0 = t1.length;
        String py = "";
        String capital = "";
        try {
            for (int i = 0; i < t0; i++) {
                t2 = PinyinHelper.toHanyuPinyinStringArray(t1[i], mOutputFormat);
                if (t2 == null || t2.length < 1 || t2[0] == null) {
                	py = py + t1[i];
                } else {
                	py = py + t2[0].toString();
                	capital = capital += t2[0].substring(0, 1);
                }
            }
        } catch (BadHanyuPinyinOutputFormatCombination e1) {
            e1.printStackTrace();
            return false;
        }
        mSpell = py.trim();
        mCapital = capital.trim();
        return true;
	}
	public String getSpell() {
		return mSpell;
	}
	public String getCapital() {
		return mCapital;
	}

	public static String toPinYin(String hanzhis){
		CharSequence s = hanzhis;
        char [] hanzhi = new char[s.length()];
        for(int i = 0; i < s.length(); i++){
            hanzhi[i] = s.charAt(i);
        }
        
        char [] t1 = hanzhi; 
        String[] t2 = new String[s.length()];
        net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat t3 = new
        HanyuPinyinOutputFormat();
        t3.setCaseType(HanyuPinyinCaseType.UPPERCASE);
        t3.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        t3.setVCharType(HanyuPinyinVCharType.WITH_V);
        
        int t0 = t1.length;
        String py = "";
        try {
            for (int i = 0; i < t0; i++) {
                t2 = PinyinHelper.toHanyuPinyinStringArray(t1[i], t3);
                if (t2 == null || t2.length < 1 || t2[0] == null)py = py + t1[i];
                else py = py + t2[0].toString();
            }
        } catch (BadHanyuPinyinOutputFormatCombination e1) {
            e1.printStackTrace();
        }
        
        return py.trim();
	}
}
