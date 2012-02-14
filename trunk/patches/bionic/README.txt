Using the patch

Prerequisities:
* build tools (GNU Make + etc)

1. Checkout Android sources http://source.android.com/source/downloading.html
2. cd <Android source dir>/bionic
3. git apply <patch file path>
4. cd ..
5. PLATFORM_VERSION=2.3.6 make libc
6. cp out/target/product/generic/obj/STATIC_LIBRARIES/libc_intermediates/libc.a ./prebuilt/ndk/android-ndk-r6/platforms/android-9/arch-arm/usr/lib/libc.a

Congradulations! Patched BIONIC libc is ready to go!

Please note that you SHOULD static link against patched libc unless you understand what you do :)
