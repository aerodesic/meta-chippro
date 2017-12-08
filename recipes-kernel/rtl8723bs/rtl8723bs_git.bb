SUMMARY = "BS realtek wifi"

LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://core/rtw_ap.c;beginline=3;endline=12;md5=6590b50a1b188fc7e8837b19153acd3f"

inherit module

SRCREV = "db2c4f61d48fe3b47c167c8bcd722ce83c24aca5"
SRC_URI = " \
	git://github.com/hadess/rtl8723bs.git;protocol=https \
	file://rtl8723bs_mp.conf \
	file://rtl8723bs_mp_wifi \
	file://w \
	file://w_start \
	file://w_stop \
        file://0001-rtl8723bs-add-modules_install-and-correct-depmod.patch \
    "

S = "${WORKDIR}/git"

EXTRA_OEMAKE = "KSRC=${STAGING_KERNEL_DIR} \
                KVER=${KERNEL_VERSION} \
                SUBARCH=${ARCH} \
                ARCH=${ARCH} \
                MODDESTDIR=${D}/lib/modules/${KERNEL_VERSION}/kernel/drivers/net/wireless/ \
               "

do_install_append() {
	install -d ${D}/lib/firmware/rtlwifi
        install -m 0644 ${S}/*.bin ${D}/lib/firmware/rtlwifi
	install -d ${D}/usr/bin
	install -m 0755 ${WORKDIR}/w ${WORKDIR}/w_start ${WORKDIR}/w_stop ${D}/usr/bin
	install -d ${D}/etc/init.d
	install -m 0755 ${WORKDIR}/rtl8723bs_mp_wifi ${D}/etc/init.d
	# Add links to the run scripts
	for d in rc2.d rc3.d rc4.d rc5.d rc6.d; do
	    install -d ${D}/etc/${d}
	    ln -sf ../init.d/rtl8723bs_mp_wifi ${D}/etc/${d}/S20rtl8723bs_mp_wifi
	done
	install -m 0755 ${WORKDIR}/rtl8723bs_mp.conf ${D}/etc/modprobe.d
}

PKGV = "${KERNEL_VERSION}"

FILES_${PN} += "/lib/firmware /usr/bin /etc/init.d /etc/modprobe.d /etc/rc*.d"

