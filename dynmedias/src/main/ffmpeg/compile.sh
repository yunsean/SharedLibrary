NDK=/Users/dylan/SDK/android-sdk-osx/ndk-bundle
SYSROOT=$NDK/platforms/android-14/arch-arm/
TOOLCHAIN=$NDK/toolchains/arm-linux-androideabi-4.9/prebuilt/darwin-x86_64
PLATFORM=$NDK/platforms/android-14/arch-arm

#生成toolchain目录，这一段可以在第一次运行后注释掉
#$ANDROID_NDK/build/tools/make-standalone-toolchain.sh \
#    --toolchain=arm-linux-androideabi-4.9 \
#    --platform=android-9 --install-dir=$TOOLCHAIN 

function build_one
{
FFCONFIG_COMMON='    
    --disable-vfp
    --disable-armv5te
    --disable-armv6
    --disable-armv6t2
    --disable-neon
    --enable-asm 
    --enable-static 
    --disable-avdevice 
    --enable-swresample 
    --enable-swscale
    --enable-network --disable-everything 
    --enable-demuxer=acc,flac,h263,h264,m4v,matroska,mp3,mpegvideo,ogg,pcm_alaw,pcm_f32be,pcm_f32le,pcm_f64be,pcm_f64le,pcm_mulaw,pcm_s16be,pcm_s16le,pcm_s24be,rtsp,asf 
    --enable-demuxer=pcm_s24le,pcm_s32be,pcm_s32le,pcm_s8,pcm_u16be,pcm_u16le,pcm_u24be,pcm_u24le,pcm_u32be,pcm_u32le,pcm_u8,rtp,rtsp,sdp,wav,http_protocol,rtpdec 
    --enable-demuxer=flv,mpegts
    --enable-muxer=rtp,mpegts
    --enable-parser=aac,aac_latm,flac,h263,h264,mpeg4video,mpegaudio,mpegvideo,vorbis,vp8
    --enable-decoder=aac,aac_latm,mp3,wmalossless,wmapro,wmav1,wmav2,wmavoice,h264 
    --enable-protocol=http,https,mmsh,mmst,tcp,udp,rtp,rtmp,udp,file
    --enable-bsf=h264_mp4toannexb,aac_adtstoasc 
    --disable-doc --enable-pic  
  '

./configure \
    --target-os=linux \
    --prefix=$PREFIX \
    --enable-cross-compile \
    --enable-runtime-cpudetect \
    --disable-asm \
    --disable-doc \
    --arch=arm \
    --cc=$TOOLCHAIN/bin/arm-linux-androideabi-gcc \
    --cross-prefix=$TOOLCHAIN/bin/arm-linux-androideabi- \
    --nm=$TOOLCHAIN/bin/arm-linux-androideabi-nm \
    --sysroot=$PLATFORM \
    $FFCONFIG_COMMON \
    --extra-cflags="$EXTRA_CFLAGS $ADDI_CFLAGS" \
    --extra-ldflags="$ADDI_LDFLAGS"

make clean
#make -j $(nproc)
make
make install
}


CPU=arm
PREFIX=$(pwd)/android/$CPU 
ADDI_CFLAGS="-marm"
build_one

CPU=armv7a
PREFIX=$(pwd)/android/$CPU 
ADDI_CFLAGS="-marm"
build_one

CPU=x86
PREFIX=$(pwd)/android/$CPU 
ADDI_CFLAGS="-marm"
build_one

# 这段解释见后文
# $TOOLCHAIN/bin/arm-linux-androideabi-ld -rpath-link=$PLATFORM/usr/lib -L$PLATFORM/usr/lib -L$PREFIX/lib -soname libffmpeg.so -shared -nostdlib -Bsymbolic --whole-archive --no-undefined -o $PREFIX/libffmpeg.so \
#     android-lib/lib/libx264.a \
#     libavcodec/libavcodec.a \
#     libavfilter/libavfilter.a \
#     libswresample/libswresample.a \
#     libavformat/libavformat.a \
#     libavutil/libavutil.a \
#     libswscale/libswscale.a \
#     libpostproc/libpostproc.a \
#     libavdevice/libavdevice.a \
#     -lc -lm -lz -ldl -llog --dynamic-linker=/system/bin/linker $TOOLCHAIN/lib/gcc/arm-linux-androideabi/4.9.x/libgcc.a  
$TOOLCHAIN/bin/arm-linux-androideabi-ld -rpath-link=$PLATFORM/usr/lib -L$PLATFORM/usr/lib -L$PREFIX/lib -soname libffmpeg.so -shared -nostdlib -Bsymbolic --whole-archive --no-undefined -o $PREFIX/libffmpeg.so \
    libavcodec/libavcodec.a \
    libswresample/libswresample.a \
    libavformat/libavformat.a \
    libavutil/libavutil.a \
    libswscale/libswscale.a \
    -lc -lm -lz -ldl -llog --dynamic-linker=/system/bin/linker $TOOLCHAIN/lib/gcc/arm-linux-androideabi/4.9.x/libgcc.a  

