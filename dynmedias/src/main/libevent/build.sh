export NDKROOT="/Volumes/Data/Android/android-ndk-r12-darwin"
PREBUILT=$NDKROOT/toolchains/arm-linux-androideabi-4.9/prebuilt/darwin-x86_64

./configure \
 --host=arm-linux-androideabi \
 --prefix=$PWD/install \
 CC=$PREBUILT/bin/arm-linux-androideabi-gcc \
 LD=$PREBUILT/bin/arm-linux-androideabi-ld \
 CPPFLAGS="-I$NDKROOT/platforms/android-14/arch-arm/usr/include/" \
 CFLAGS="-nostdlib" \
 LDFLAGS="-Wl,-rpath-link=$NDKROOT/platforms/android-14/arch-arm/usr/lib/ -L$NDKROOT/platforms/android-14/arch-arm/usr/lib/" \
 LIBS="-lc -lgcc -L$NDKROOT/toolchains/arm-linux-androideabi-4.9/prebuilt/darwin-x86_64/lib/gcc/arm-linux-androideabi/4.9"

ln -s $NDKROOT/platforms/android-14/arch-arm/usr/lib/crtbegin_so.o
ln -s $NDKROOT/platforms/android-14/arch-arm/usr/lib/crtend_so.o

make
make install

