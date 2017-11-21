# Add mkenvimage to u-boot native items

# Install a native copy of mkenvimage
do_install_append() {
    echo D='${D}' bindir='${bindir}'
    install -d ${D}${bindir}
    install -m 0755 tools/mkenvimage ${D}${bindir}/uboot-mkenvimage
    ln -sf uboot-mkenvimage ${D}${bindir}/mkenvimage
}

