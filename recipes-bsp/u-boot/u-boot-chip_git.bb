require recipes-bsp/u-boot/u-boot.inc

DESCRIPTION = "U-Boot port for C.H.I.P. boards"
LICENSE = "GPLv2+"
LIC_FILES_CHKSUM = "file://Licenses/README;md5=a2c678cfd4a4d97135585cad908541c6"

DEPENDS += "dtc-native swig-native python-native coreutils-native"

PROVIDES += "u-boot"
RDEPENDS_${PN}_append_chippro = " uboot-config"

# EXTRA_OEMAKE+="KBUILD_VERBOSE='1' CFLAGS='${CFLAGS} -v'"

PV = "git${SRCPV}"

# SRCREV ?= "57270eca55b4e72b4af6c78ac066466dba7c6d70"
SRCREV ?= "16fa2eb95172e63820ee5f3d4052f3362a6de84e"
SRC_URI = "git://github.com/u-boot/u-boot.git \
           file://0001-Added-host-path-to-libfdt-swig-build.patch \
           "

S = "${WORKDIR}/git"

do_compile_append() {
    install ${B}/spl/${SPL_ECC_BINARY} ${B}/${SPL_ECC_BINARY}
}

COMPATIBLE_MACHINE = "chippro"

do_deploy_append() {
    install -m 644 ${B}/${SPL_ECC_BINARY} ${DEPLOYDIR}/${SPL_ECC_BINARY}-${PV}-${PR}
    ln -sf ${SPL_ECC_BINARY}-${PV}-${PR} ${DEPLOYDIR}/${SPL_ECC_BINARY}
}

