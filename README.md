This README file contains information on building the meta-chippro BSP layer.
Please see the corresponding sections below for details.


Dependencies
============

This layer depends on:

  URI: git://xxx/poky
  branch: master

  URI: git://git.openembedded.org/bitbake
  branch: master

  URI: git://git.openembedded.org/openembedded-core
  layers: meta
  branch: master

  URI: git://github.com/linux-sunxi/meta-sunxi.git
  branch: master

I use the folllowing BBLAYERS:

BBLAYERS ?= " \
  .../meta-sunxi \
  .../openembedded-core/meta \
  .../meta-openembedded/meta-oe \
  .../meta-openembedded/meta-python \
  .../meta-openembedded/meta-multimedia \
  .../poky/meta \
  .../poky/meta-poky \
  .../poky/meta-yocto-bsp \
  .../meta-chippro \
  "

You will also (for now) need the image_types.bbclass.patch in this directory and apply it to
either the openembedded-core/meta/classes/image_types.bbclass or the
poky/meta/classes/image_types.bbclass file.  This extends the image_types class to include
a multivol UBI image construction class.

