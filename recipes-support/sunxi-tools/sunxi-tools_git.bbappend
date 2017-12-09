EXTRA_OEMAKE = "all misc"

do_install_append() {
    install -m 755 ${S}/sunxi-nand-image-builder ${D}/${bindir}
}

