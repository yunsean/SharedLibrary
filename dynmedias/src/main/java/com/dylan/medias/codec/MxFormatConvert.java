package com.dylan.medias.codec;

import static android.media.MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar;
import static android.media.MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar;

public class MxFormatConvert {
    /********************************************
     * Y800: Y Y Y Y Y Y Y Y    =>Mono(8bits)       ==GREY
     * Y16 : YY YY YY YY        =>Mono(16bits)
     *
     * AYUV: AYUV AYUV          =>YUV444Packed
     *
     * UYVY: UYVY UYVY          =>YUV420Packed      ==IUYV HDYC UYNV Y422
     * Y211: YUYV YUYV          =>YUV420Packed      ==YUY2 YUYV	YUNV
     *
     * I420: YYYYYYYY UU VV     =>YUV420P
     * YV12: YYYYYYYY VV UU     =>YUV420P
     * NV12: YYYYYYYY UVUV      =>YUV420SP
     * NV21: YYYYYYYY VUVU      =>YUV420SP
     *
    ********************************************/
    public final static int FOURCC_I420 = MakeFOURCC('I', '4', '2', '0');
    public final static int FOURCC_YV12 = MakeFOURCC('Y', 'V', '1', '2');
    public final static int FOURCC_NV12 = MakeFOURCC('N', 'V', '1', '2');
    public final static int FOURCC_NV21 = MakeFOURCC('N', 'V', '2', '1');
    public final static int FOURCC_YUY2 = MakeFOURCC('Y', 'U', 'Y', '2');
    public final static int MakeFOURCC(char d, char c, char b, char a) {
        return (int)(a & 0xff) | (int)((b << 8) & 0xff00) | (int)((c << 16) & 0xff0000) | (int)((d << 24) & 0xff000000);
    }
    public static class CodecCapabilities {
        public final static int toFOURCC(int codec) {
            switch (codec) {
                case COLOR_FormatYUV420Planar:
                    return FOURCC_YV12;
                case COLOR_FormatYUV420SemiPlanar:
                    return FOURCC_NV21;
                default:
                    return 0;
            }
        }
        public final static int fromFOURCC(int fourcc) {       
            if (fourcc == FOURCC_YV12) return COLOR_FormatYUV420Planar;
            else if (fourcc == FOURCC_NV21) return COLOR_FormatYUV420SemiPlanar;
            else return 0;
        }
    }
    public static class ImageFormat {
        public final static int toFOURCC(int format) {
            switch (format) {
                case android.graphics.ImageFormat.YV12:
                    return FOURCC_YV12;
                case android.graphics.ImageFormat.NV21:
                    return FOURCC_NV21;
                case android.graphics.ImageFormat.YUY2:
                    return FOURCC_YUY2;
                default:
                    return 0;
            }
        }
        public final static int fromFOURCC(int fourcc) {
            if (fourcc == FOURCC_YV12) return android.graphics.ImageFormat.YV12;
            else if (fourcc == FOURCC_NV12) return android.graphics.ImageFormat.NV21;
            else if (fourcc == FOURCC_YUY2) return android.graphics.ImageFormat.YUY2;
            else return 0;
        }
    }
}
