require recipes-bsp/u-boot/u-boot.inc

DESCRIPTION = "U-Boot port for C.H.I.P. boards"
LICENSE = "GPLv2+"
LIC_FILES_CHKSUM = "file://Licenses/README;md5=a2c678cfd4a4d97135585cad908541c6"

DEPENDS += "dtc-native swig-native python-native coreutils-native u-boot-mkimage-native"

COMPATIBLE_MACHINE = "chippro"

PROVIDES += "u-boot"
RDEPENDS_${PN}_append_chippro = " image-flasher"

IMG_NAME ?= "u-boot.img-${MACHINE}-${PN}-${PV}.bin"
ENV_IMAGE ?= "${UBOOT_ENV_NAME}-${MACHINE}-${PN}-${PV}.bin"

PV = "git${SRCPV}"

# Same version as u-boot-mkimage-native
SRCREV = "0c4d24823ed28c94dae56a10a66687c69b70c1d1"

SRC_URI = " \
	git://github.com/u-boot/u-boot.git \
	file://CHIP_pro_defconfig \
	file://0001-Added-host-path-to-libfdt-build.patch \
	file://0002-Add-booting-from-UBI-volume-zImages.patch \
	file://0003-Change-mutex_is_locked-to-return-TRUE.patch \
    "

S = "${WORKDIR}/git"

do_compile_prepend() {
    # Install the local copy of the defconfig
    install ${WORKDIR}/CHIP_pro_defconfig ${S}/configs/
}
 
do_deploy_append() {

    install ${B}/spl/${SPL_ECC_BINARY} ${DEPLOYDIR}
    install ${B}/spl/sunxi-spl.bin ${DEPLOYDIR}
    install ${B}/spl/u-boot-spl.bin ${DEPLOYDIR}

    # Extract environment from u-boot compile (so that items from the u-boot config get through)
    ${OBJCOPY} -O binary -j ".rodata.default_environment" ${B}/env/common.o ${B}/rawenv.bin

    # Convert NUL bytes to newline
    tr "\0" "\n" <${B}/rawenv.bin >${B}/${ENV_IMAGE}.txt

    install -d ${DEPLOYDIR}

    install ${B}/${ENV_IMAGE}.txt ${DEPLOYDIR}
    ln -sf ${ENV_IMAGE}.txt ${DEPLOYDIR}/${UBOOT_ENV_NAME}.txt

    # Create the stand-alone environment from this text
    mkenvimage -s ${ENV_IMAGE_SIZE} -o ${B}/${ENV_IMAGE} ${DEPLOYDIR}/${UBOOT_ENV_NAME}.txt
    install ${B}/${ENV_IMAGE} ${DEPLOYDIR}
    ln -sf ${ENV_IMAGE} ${DEPLOYDIR}/${UBOOT_ENV_NAME}

    # Install the aggregate image
    install ${B}/u-boot.img ${DEPLOYDIR}/${IMG_NAME}
    ln -sf ${IMG_NAME} ${DEPLOYDIR}/u-boot.img

    rm ${B}/rawenv.bin
}

