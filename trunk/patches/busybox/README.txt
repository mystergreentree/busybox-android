Using the patch

Prerequisities:
* build tools (GNU Make + etc)
* checked out Android sources
* patched and built BIONIC libc

1. Checkout busybox sources. http://busybox.net/source.html
2. Apply patch to busybox source dir (git apply <patch file path>)
3. Copy .config to busybox source dir
4. Replace /var/data/work/android-src with your Android source path in .config and examples/android-build
5. Tune .config if necessary
6. Invoke examples/android-build

Enjoy!
