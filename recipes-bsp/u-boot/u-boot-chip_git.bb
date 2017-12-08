require recipes-bsp/u-boot/u-boot.inc

DESCRIPTION = "U-Boot port for C.H.I.P. boards"
LICENSE = "GPLv2+"
LIC_FILES_CHKSUM = "file://Licenses/README;md5=a2c678cfd4a4d97135585cad908541c6"

DEPENDS += "dtc-native swig-native python-native coreutils-native u-boot-mkimage-native"

COMPATIBLE_MACHINE = "chippro"

PROVIDES += "u-boot"
RDEPENDS_${PN}_append_chippro = " image-flasher"


PV = "git${SRCPV}"

# SRCREV ?= "0c4d24823ed28c94dae56a10a66687c69b70c1d1"
# Same version as u-boot-mkimage-native
SRCREV = "0c4d24823ed28c94dae56a10a66687c69b70c1d1"

SRC_URI = "git://github.com/u-boot/u-boot.git \
           file://CHIP_pro_defconfig \
           file://uboot.script \
           file://0001-Added-host-path-to-libfdt-build.patch \
           file://0002-Add-booting-from-UBI-volume-zImages.patch \
           file://0003-Change-mutex_is_locked-to-return-TRUE-rather-than-FA.patch \
           "


S = "${WORKDIR}/git"

do_configure_prepend() {
    cp ${WORKDIR}/CHIP_pro_defconfig ${S}/configs/
}

do_compile_prepend() {
    install -d -m 755 ${DEPLOYDIR}
    mkimage -A arm -T script -C none -n "Flash" -d "${WORKDIR}/uboot.script" "${DEPLOYDIR}/boot.scr"
}

do_compile_append() {
    # Create padded version of u-boot-dtb.bin
    ${OBJCOPY} 			\
	--pad-to=0xc0000	\
	--gap-fill=0xFF		\
	-j .text		\
	-j .secure_text		\
	-j .rodata		\
	-j .hash		\
	-j .data		\
	-j .got			\
	-j .got.plt		\
	-j .u_boot_list		\
	-j .rel_dyn		\
	--gap-fill=0xFF		\
	-I binary		\
	-O binary		\
	${B}/u-boot-dtb.bin	\
	${B}/${UBOOT_BINARY}

    install ${B}/spl/${SPL_ECC_BINARY} ${B}/${SPL_ECC_BINARY}
    # Move the file if necessary
    if [ -e ${B}/spl/${SPL_BINARY} ] ; then
      install ${B}/spl/${SPL_BINARY} ${B}/${SPL_BINARY}
    fi
}

#
# Install some things left out of base module
#
do_deploy_append() {
    install -m 644 ${B}/${SPL_ECC_BINARY} ${DEPLOYDIR}/${SPL_ECC_BINARY}-${PV}-${PR}
    ln -sf ${SPL_ECC_BINARY}-${PV}-${PR} ${DEPLOYDIR}/${SPL_ECC_BINARY}

    # Extract environment from u-boot compile
    ${OBJCOPY} -O binary -j ".rodata.default_environment" ${B}/env/common.o ${B}/rawenv.bin

    # Convert NUL bytes to newline
    tr "\0" "\n" <${B}/rawenv.bin >${B}/rawenv.txt

    mkenvimage -s ${ENV_IMAGE_SIZE} -o ${DEPLOYDIR}/${UBOOT_ENV_NAME}-${PV}-${PR} ${B}/rawenv.txt
    ln -sf ${UBOOT_ENV_NAME}-${PV}-${PR} ${DEPLOYDIR}/${UBOOT_ENV_NAME}

    # Remove before flight
    # rm ${B}/rawenv.bin ${B}/rawenv.txt

    install -m 644 ${B}/${UBOOT_BINARY} ${DEPLOYDIR}/${UBOOT_BINARY}-${PV}-${PR}
    ln -sf ${UBOOT_BINARY}-${PV}-${PR} ${DEPLOYDIR}/${UBOOT_BINARY}
}

