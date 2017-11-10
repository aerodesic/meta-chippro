require recipes-bsp/u-boot/u-boot.inc

DESCRIPTION = "U-Boot port for C.H.I.P. boards"
LICENSE = "GPLv2+"
LIC_FILES_CHKSUM = "file://Licenses/README;md5=a2c678cfd4a4d97135585cad908541c6"

DEPENDS += "dtc-native swig-native python-native coreutils-native"
PROVIDES += "u-boot"
RDEPENDS_${PN}_append_chippro = " chip-u-boot-scr"

EXTRA_OEMAKE+="KBUILD_VERBOSE='1' CFLAGS='${CFLAGS} -v'"

PV = "git${SRCPV}"
# SPL_UBOOT_BINARY = "u-boot-sunxi-with-spl.bin"
# SPL_UBOOT_BINARY = "u-boot-spl.bin"

SRCREV ?= "57270eca55b4e72b4af6c78ac066466dba7c6d70"
SRC_URI = "git://github.com/u-boot/u-boot.git \
           file://0001-Added-host-path-to-libfdt-swig-build.patch \
           "

S = "${WORKDIR}/git"

do_compile_append() {
    install ${B}/spl/${SPL_ECC_BINARY} ${B}/${SPL_ECC_BINARY}
    install ${B}/spl/${SPL_BINARY} ${B}/${SPL_BINARY}
}

COMPATIBLE_MACHINE = "chippro"

do_deploy_append() {
    echo SPL_ECC_BINARY '${SPL_ECC_BINARY}'
    echo SPL_UBOOT_BINARY '${SPL_UBOOT_BINARY}'
    install -m 644 ${B}/${SPL_ECC_BINARY} ${DEPLOYDIR}/${SPL_ECC_BINARY}-${PV}-${PR}
    ln -sf ${SPL_ECC_BINARY}-${PV}-${PR} ${DEPLOYDIR}/${SPL_ECC_BINARY}
    install -m 644 ${B}/${SPL_UBOOT_BINARY} ${DEPLOYDIR}/${SPL_UBOOT_BINARY}
}
