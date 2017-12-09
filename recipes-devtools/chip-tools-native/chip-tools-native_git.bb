DESCRIPTION = "Produce chip tools for NTC devices"
LICENSE = "GPLv2+"
LIC_FILES_CHKSUM = "file://LICENSE;md5=84b16842f639f6ed1f43717a1f484179"

RDEPENDS_chip-tools-native = "bash"

# DEPENDS += "u-boot-mkimage-native"

SECTION = "devel"

inherit allarch native

PV = "git${SRCPV}"

SRCREV = "f79fe329d9299fdec17478844afe960acbcc8fad"
SRC_URI = "git://github.com/NextThingCo/CHIP-tools.git;branch=chip/stable \
           file://0001-modified-to-handle-image_class-ubi-builds.patch \
           "

S = "${WORKDIR}/git"

do_install() {
    install -d ${D}/${bindir}
    install -m 755 ${S}/*.sh ${D}/${bindir}
}

FILES_${PN} = "${bindir}/*.sh"

