require recipes-bsp/u-boot/u-boot.inc

DESCRIPTION = "U-Boot port for C.H.I.P. boards"
LICENSE = "GPLv2+"
LIC_FILES_CHKSUM = "file://Licenses/README;md5=0507cd7da8e7ad6d6701926ec9b84c95"

DEPENDS += "dtc-native swig-native python-native coreutils-native"

PROVIDES += "u-boot"
RDEPENDS_${PN}_append_chippro = " uboot-config"

BRANCH = "ww/2016.01/next"
UBOOT_VERSION = "ww-2016.01-next"
PV = "${UBOOT_VERSION}-git${SRCPV}"

SRCREV="2fa5547d620ef2885f680986891c3feb425dbfe6"
SRC_URI = " \
	git://github.com/NextThingCo/CHIP-u-boot.git;branch=${BRANCH} \
	file://CHIP_pro_defconfig \
        "

S = "${WORKDIR}/git"

do_compile_prepend() {
    cp ${WORKDIR}/CHIP_pro_defconfig ${S}/configs/
}

do_compile_append() {
    install ${B}/spl/${SPL_ECC_BINARY} ${B}/${SPL_ECC_BINARY}
}

COMPATIBLE_MACHINE = "chippro"

do_deploy_append() {
    install -m 644 ${B}/${SPL_ECC_BINARY} ${DEPLOYDIR}/${SPL_ECC_BINARY}-${PV}-${PR}
    ln -sf ${SPL_ECC_BINARY}-${PV}-${PR} ${DEPLOYDIR}/${SPL_ECC_BINARY}
}

