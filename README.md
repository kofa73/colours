I used Java 19. You can get it at https://adoptium.net/en-GB/temurin/releases/ if you don't have it.
The build tool is Maven: https://maven.apache.org/download.cgi

# Compile

`mvn clean package`

# Use

For instructions:

`java -jar target/colours-0.0.1-SNAPSHOT-jar-with-dependencies.jar`

To process a *16-bit linear Rec2020* file, and write it as an sRGB file with TRC applied, use the following command (PNG
and TIFF are supported, maybe others too -- this comes from Java, I did not spend time on I/O). Profiles are ignored,
data is assumed to be in linear Rec2020. Floating point TIFFs are not supported.
Note that only a base file name is needed for the output; the name of the mapper and the `.png` extension will be
appended.

`java -jar target/colours-0.0.1-SNAPSHOT-jar-with-dependencies.jar ~/tmp/dt-test/darktable_exported/0L0A3314.tif ~/tmp/dt-test/darktable_exported/0L0A3314 1 2 3 4 5 6`

The result is:

```text
Using RgbClippingGamutMapper
Wrote /home/kofa/tmp/dt-test/darktable_exported/0L0A3314-RgbClippingGamutMapper.png
Using BwFromLGamutMapper
Wrote /home/kofa/tmp/dt-test/darktable_exported/0L0A3314-BwFromLGamutMapper.png
Using DesaturatingLchBasedGamutMapperLchAb
Wrote /home/kofa/tmp/dt-test/darktable_exported/0L0A3314-DesaturatingLchBasedGamutMapperLchAb.png
Using DesaturatingLchBasedGamutMapperLchUv
Wrote /home/kofa/tmp/dt-test/darktable_exported/0L0A3314-DesaturatingLchBasedGamutMapperLchUv.png
Using ChromaClippingLchBasedGamutMapperLchAb
Wrote /home/kofa/tmp/dt-test/darktable_exported/0L0A3314-ChromaClippingLchBasedGamutMapperLchAb.png
Using ChromaClippingLchBasedGamutMapperLchUv
Wrote /home/kofa/tmp/dt-test/darktable_exported/0L0A3314-ChromaClippingLchBasedGamutMapperLchUv.png
```
