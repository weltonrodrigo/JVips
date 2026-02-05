/*
  Copyright (c) 2020 Criteo
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
      http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

#include <stdlib.h>
#include <string.h>
#include <stdio.h>
#include <jni.h>
#include <vips/vips.h>

#include "VipsException.h"
#include "VipsEnumTest.h"

#define BUF_SIZE 4096

static void
assertEqualsNativeEnumValue(JNIEnv *env, const int expected, const char *className, const char *name)
{
    char sig[BUF_SIZE] = { 0 };
    sprintf(sig, "L%s;", className);
    jclass cls = (*env)->FindClass(env, className);
    jfieldID id = (*env)->GetStaticFieldID(env, cls , name, sig);
    jobject obj = (*env)->GetStaticObjectField(env, cls, id);
    jmethodID mid = (*env)->GetMethodID(env, cls , "getValue", "()I");
    jint actual = (*env)->CallIntMethod(env, obj, mid);

    if (expected != actual)
    {
        char msg[BUF_SIZE] = { 0 };
        sprintf(msg, "%s:%s is not equal to expected value (%d)", className, name, expected);
        throwVipsException(env, msg);
    }
    return;
}

JNIEXPORT void JNICALL
Java_com_criteo_vips_VipsEnumTest_TestNativeEnums(JNIEnv *env, jclass c)
{
    // VipsAccess
    assertEqualsNativeEnumValue(env, VIPS_ACCESS_RANDOM, "com/criteo/vips/enums/VipsAccess", "Random");
    assertEqualsNativeEnumValue(env, VIPS_ACCESS_SEQUENTIAL, "com/criteo/vips/enums/VipsAccess", "Sequential");
    assertEqualsNativeEnumValue(env, VIPS_ACCESS_SEQUENTIAL_UNBUFFERED, "com/criteo/vips/enums/VipsAccess", "SequentialUnbuffered");
    // VipsAlign
    assertEqualsNativeEnumValue(env, VIPS_ALIGN_LOW, "com/criteo/vips/enums/VipsAlign", "Low");
    assertEqualsNativeEnumValue(env, VIPS_ALIGN_CENTRE, "com/criteo/vips/enums/VipsAlign", "Centre");
    assertEqualsNativeEnumValue(env, VIPS_ALIGN_HIGH, "com/criteo/vips/enums/VipsAlign", "High");
    // VipsAngle
    assertEqualsNativeEnumValue(env, VIPS_ANGLE_D0, "com/criteo/vips/enums/VipsAngle", "D0");
    assertEqualsNativeEnumValue(env, VIPS_ANGLE_D90, "com/criteo/vips/enums/VipsAngle", "D90");
    assertEqualsNativeEnumValue(env, VIPS_ANGLE_D180, "com/criteo/vips/enums/VipsAngle", "D180");
    assertEqualsNativeEnumValue(env, VIPS_ANGLE_D270, "com/criteo/vips/enums/VipsAngle", "D270");
    // VipsAngle45
    assertEqualsNativeEnumValue(env, VIPS_ANGLE45_D0, "com/criteo/vips/enums/VipsAngle45", "D0");
    assertEqualsNativeEnumValue(env, VIPS_ANGLE45_D45, "com/criteo/vips/enums/VipsAngle45", "D45");
    assertEqualsNativeEnumValue(env, VIPS_ANGLE45_D90, "com/criteo/vips/enums/VipsAngle45", "D90");
    assertEqualsNativeEnumValue(env, VIPS_ANGLE45_D135, "com/criteo/vips/enums/VipsAngle45", "D135");
    assertEqualsNativeEnumValue(env, VIPS_ANGLE45_D180, "com/criteo/vips/enums/VipsAngle45", "D180");
    assertEqualsNativeEnumValue(env, VIPS_ANGLE45_D225, "com/criteo/vips/enums/VipsAngle45", "D225");
    assertEqualsNativeEnumValue(env, VIPS_ANGLE45_D270, "com/criteo/vips/enums/VipsAngle45", "D270");
    assertEqualsNativeEnumValue(env, VIPS_ANGLE45_D315, "com/criteo/vips/enums/VipsAngle45", "D315");
    // VipsArgumentFlags
    assertEqualsNativeEnumValue(env, VIPS_ARGUMENT_NONE, "com/criteo/vips/enums/VipsArgumentFlags", "ArgumentNone");
    assertEqualsNativeEnumValue(env, VIPS_ARGUMENT_REQUIRED, "com/criteo/vips/enums/VipsArgumentFlags", "ArgumentRequired");
    assertEqualsNativeEnumValue(env, VIPS_ARGUMENT_CONSTRUCT, "com/criteo/vips/enums/VipsArgumentFlags", "ArgumentConstruct");
    assertEqualsNativeEnumValue(env, VIPS_ARGUMENT_SET_ONCE, "com/criteo/vips/enums/VipsArgumentFlags", "ArgumentSetOnce");
    assertEqualsNativeEnumValue(env, VIPS_ARGUMENT_SET_ALWAYS, "com/criteo/vips/enums/VipsArgumentFlags", "ArgumentSetAlways");
    assertEqualsNativeEnumValue(env, VIPS_ARGUMENT_INPUT, "com/criteo/vips/enums/VipsArgumentFlags", "ArgumentInput");
    assertEqualsNativeEnumValue(env, VIPS_ARGUMENT_OUTPUT, "com/criteo/vips/enums/VipsArgumentFlags", "ArgumentOutput");
    assertEqualsNativeEnumValue(env, VIPS_ARGUMENT_DEPRECATED, "com/criteo/vips/enums/VipsArgumentFlags", "ArgumentDeprecated");
    assertEqualsNativeEnumValue(env, VIPS_ARGUMENT_MODIFY, "com/criteo/vips/enums/VipsArgumentFlags", "ArgumentModify");
    assertEqualsNativeEnumValue(env, VIPS_ARGUMENT_NON_HASHABLE, "com/criteo/vips/enums/VipsArgumentFlags", "ArgumentNonHashable");
    // VipsBandFormat
    assertEqualsNativeEnumValue(env, VIPS_FORMAT_NOTSET, "com/criteo/vips/enums/VipsBandFormat", "FormatNotset");
    assertEqualsNativeEnumValue(env, VIPS_FORMAT_UCHAR, "com/criteo/vips/enums/VipsBandFormat", "FormatUchar");
    assertEqualsNativeEnumValue(env, VIPS_FORMAT_CHAR, "com/criteo/vips/enums/VipsBandFormat", "FormatChar");
    assertEqualsNativeEnumValue(env, VIPS_FORMAT_USHORT, "com/criteo/vips/enums/VipsBandFormat", "FormatUshort");
    assertEqualsNativeEnumValue(env, VIPS_FORMAT_SHORT, "com/criteo/vips/enums/VipsBandFormat", "FormatShort");
    assertEqualsNativeEnumValue(env, VIPS_FORMAT_UINT, "com/criteo/vips/enums/VipsBandFormat", "FormatUint");
    assertEqualsNativeEnumValue(env, VIPS_FORMAT_INT, "com/criteo/vips/enums/VipsBandFormat", "FormatInt");
    assertEqualsNativeEnumValue(env, VIPS_FORMAT_FLOAT, "com/criteo/vips/enums/VipsBandFormat", "FormatFloat");
    assertEqualsNativeEnumValue(env, VIPS_FORMAT_COMPLEX, "com/criteo/vips/enums/VipsBandFormat", "FormatComplex");
    assertEqualsNativeEnumValue(env, VIPS_FORMAT_DOUBLE, "com/criteo/vips/enums/VipsBandFormat", "FormatDouble");
    assertEqualsNativeEnumValue(env, VIPS_FORMAT_DPCOMPLEX, "com/criteo/vips/enums/VipsBandFormat", "FormatDpcomplex");
    // VipsBlendMode
    assertEqualsNativeEnumValue(env, VIPS_BLEND_MODE_CLEAR, "com/criteo/vips/enums/VipsBlendMode", "Clear");
    assertEqualsNativeEnumValue(env, VIPS_BLEND_MODE_SOURCE, "com/criteo/vips/enums/VipsBlendMode", "Source");
    assertEqualsNativeEnumValue(env, VIPS_BLEND_MODE_OVER, "com/criteo/vips/enums/VipsBlendMode", "Over");
    assertEqualsNativeEnumValue(env, VIPS_BLEND_MODE_IN, "com/criteo/vips/enums/VipsBlendMode", "In");
    assertEqualsNativeEnumValue(env, VIPS_BLEND_MODE_OUT, "com/criteo/vips/enums/VipsBlendMode", "Out");
    assertEqualsNativeEnumValue(env, VIPS_BLEND_MODE_ATOP, "com/criteo/vips/enums/VipsBlendMode", "Atop");
    assertEqualsNativeEnumValue(env, VIPS_BLEND_MODE_DEST, "com/criteo/vips/enums/VipsBlendMode", "Dest");
    assertEqualsNativeEnumValue(env, VIPS_BLEND_MODE_DEST_OVER, "com/criteo/vips/enums/VipsBlendMode", "DestOver");
    assertEqualsNativeEnumValue(env, VIPS_BLEND_MODE_DEST_IN, "com/criteo/vips/enums/VipsBlendMode", "DestIn");
    assertEqualsNativeEnumValue(env, VIPS_BLEND_MODE_DEST_OUT, "com/criteo/vips/enums/VipsBlendMode", "DestOut");
    assertEqualsNativeEnumValue(env, VIPS_BLEND_MODE_DEST_ATOP, "com/criteo/vips/enums/VipsBlendMode", "DestAtop");
    assertEqualsNativeEnumValue(env, VIPS_BLEND_MODE_XOR, "com/criteo/vips/enums/VipsBlendMode", "Xor");
    assertEqualsNativeEnumValue(env, VIPS_BLEND_MODE_ADD, "com/criteo/vips/enums/VipsBlendMode", "Add");
    assertEqualsNativeEnumValue(env, VIPS_BLEND_MODE_SATURATE, "com/criteo/vips/enums/VipsBlendMode", "Saturate");
    assertEqualsNativeEnumValue(env, VIPS_BLEND_MODE_MULTIPLY, "com/criteo/vips/enums/VipsBlendMode", "Multiply");
    assertEqualsNativeEnumValue(env, VIPS_BLEND_MODE_SCREEN, "com/criteo/vips/enums/VipsBlendMode", "Screen");
    assertEqualsNativeEnumValue(env, VIPS_BLEND_MODE_OVERLAY, "com/criteo/vips/enums/VipsBlendMode", "Overlay");
    assertEqualsNativeEnumValue(env, VIPS_BLEND_MODE_DARKEN, "com/criteo/vips/enums/VipsBlendMode", "Darken");
    assertEqualsNativeEnumValue(env, VIPS_BLEND_MODE_LIGHTEN, "com/criteo/vips/enums/VipsBlendMode", "Lighten");
    assertEqualsNativeEnumValue(env, VIPS_BLEND_MODE_COLOUR_DODGE, "com/criteo/vips/enums/VipsBlendMode", "ColourDodge");
    assertEqualsNativeEnumValue(env, VIPS_BLEND_MODE_COLOUR_BURN, "com/criteo/vips/enums/VipsBlendMode", "ColourBurn");
    assertEqualsNativeEnumValue(env, VIPS_BLEND_MODE_HARD_LIGHT, "com/criteo/vips/enums/VipsBlendMode", "HardLight");
    assertEqualsNativeEnumValue(env, VIPS_BLEND_MODE_SOFT_LIGHT, "com/criteo/vips/enums/VipsBlendMode", "SoftLight");
    assertEqualsNativeEnumValue(env, VIPS_BLEND_MODE_DIFFERENCE, "com/criteo/vips/enums/VipsBlendMode", "Difference");
    assertEqualsNativeEnumValue(env, VIPS_BLEND_MODE_EXCLUSION, "com/criteo/vips/enums/VipsBlendMode", "Exclusion");
    // VipsCoding
    assertEqualsNativeEnumValue(env, VIPS_CODING_ERROR, "com/criteo/vips/enums/VipsCoding", "Error");
    assertEqualsNativeEnumValue(env, VIPS_CODING_NONE, "com/criteo/vips/enums/VipsCoding", "None");
    assertEqualsNativeEnumValue(env, VIPS_CODING_LABQ, "com/criteo/vips/enums/VipsCoding", "Labq");
    assertEqualsNativeEnumValue(env, VIPS_CODING_RAD, "com/criteo/vips/enums/VipsCoding", "Rad");
    // VipsCombine
    assertEqualsNativeEnumValue(env, VIPS_COMBINE_MAX, "com/criteo/vips/enums/VipsCombine", "Max");
    assertEqualsNativeEnumValue(env, VIPS_COMBINE_SUM, "com/criteo/vips/enums/VipsCombine", "Sum");
    assertEqualsNativeEnumValue(env, VIPS_COMBINE_MIN, "com/criteo/vips/enums/VipsCombine", "Min");
    // VipsCombineMode
    assertEqualsNativeEnumValue(env, VIPS_COMBINE_MODE_SET, "com/criteo/vips/enums/VipsCombineMode", "Set");
    assertEqualsNativeEnumValue(env, VIPS_COMBINE_MODE_ADD, "com/criteo/vips/enums/VipsCombineMode", "Add");
    // VipsCompassDirection
    assertEqualsNativeEnumValue(env, VIPS_COMPASS_DIRECTION_CENTRE, "com/criteo/vips/enums/VipsCompassDirection", "Centre");
    assertEqualsNativeEnumValue(env, VIPS_COMPASS_DIRECTION_NORTH, "com/criteo/vips/enums/VipsCompassDirection", "North");
    assertEqualsNativeEnumValue(env, VIPS_COMPASS_DIRECTION_EAST, "com/criteo/vips/enums/VipsCompassDirection", "East");
    assertEqualsNativeEnumValue(env, VIPS_COMPASS_DIRECTION_SOUTH, "com/criteo/vips/enums/VipsCompassDirection", "South");
    assertEqualsNativeEnumValue(env, VIPS_COMPASS_DIRECTION_WEST, "com/criteo/vips/enums/VipsCompassDirection", "West");
    assertEqualsNativeEnumValue(env, VIPS_COMPASS_DIRECTION_NORTH_EAST, "com/criteo/vips/enums/VipsCompassDirection", "NorthEast");
    assertEqualsNativeEnumValue(env, VIPS_COMPASS_DIRECTION_SOUTH_EAST, "com/criteo/vips/enums/VipsCompassDirection", "SouthEast");
    assertEqualsNativeEnumValue(env, VIPS_COMPASS_DIRECTION_SOUTH_WEST, "com/criteo/vips/enums/VipsCompassDirection", "SouthWest");
    assertEqualsNativeEnumValue(env, VIPS_COMPASS_DIRECTION_NORTH_WEST, "com/criteo/vips/enums/VipsCompassDirection", "NorthWest");
    // VipsDemandStyle
    assertEqualsNativeEnumValue(env, VIPS_DEMAND_STYLE_ERROR, "com/criteo/vips/enums/VipsDemandStyle", "Error");
    assertEqualsNativeEnumValue(env, VIPS_DEMAND_STYLE_SMALLTILE, "com/criteo/vips/enums/VipsDemandStyle", "Smalltile");
    assertEqualsNativeEnumValue(env, VIPS_DEMAND_STYLE_FATSTRIP, "com/criteo/vips/enums/VipsDemandStyle", "Fatstrip");
    assertEqualsNativeEnumValue(env, VIPS_DEMAND_STYLE_THINSTRIP, "com/criteo/vips/enums/VipsDemandStyle", "Thinstrip");
    assertEqualsNativeEnumValue(env, VIPS_DEMAND_STYLE_ANY, "com/criteo/vips/enums/VipsDemandStyle", "Any");
    // VipsDirection
    assertEqualsNativeEnumValue(env, VIPS_DIRECTION_HORIZONTAL, "com/criteo/vips/enums/VipsDirection", "Horizontal");
    assertEqualsNativeEnumValue(env, VIPS_DIRECTION_VERTICAL, "com/criteo/vips/enums/VipsDirection", "Vertical");
    // VipsExtend
    assertEqualsNativeEnumValue(env, VIPS_EXTEND_BLACK, "com/criteo/vips/enums/VipsExtend", "Black");
    assertEqualsNativeEnumValue(env, VIPS_EXTEND_COPY, "com/criteo/vips/enums/VipsExtend", "Copy");
    assertEqualsNativeEnumValue(env, VIPS_EXTEND_REPEAT, "com/criteo/vips/enums/VipsExtend", "Repeat");
    assertEqualsNativeEnumValue(env, VIPS_EXTEND_MIRROR, "com/criteo/vips/enums/VipsExtend", "Mirror");
    assertEqualsNativeEnumValue(env, VIPS_EXTEND_WHITE, "com/criteo/vips/enums/VipsExtend", "White");
    assertEqualsNativeEnumValue(env, VIPS_EXTEND_BACKGROUND, "com/criteo/vips/enums/VipsExtend", "Background");
    // VipsFailOn
    assertEqualsNativeEnumValue(env, VIPS_FAIL_ON_NONE, "com/criteo/vips/enums/VipsFailOn", "None");
    assertEqualsNativeEnumValue(env, VIPS_FAIL_ON_TRUNCATED, "com/criteo/vips/enums/VipsFailOn", "Truncated");
    assertEqualsNativeEnumValue(env, VIPS_FAIL_ON_ERROR, "com/criteo/vips/enums/VipsFailOn", "Error");
    assertEqualsNativeEnumValue(env, VIPS_FAIL_ON_WARNING, "com/criteo/vips/enums/VipsFailOn", "Warning");
    // VipsForeignCoding
    assertEqualsNativeEnumValue(env, VIPS_FOREIGN_CODING_NONE, "com/criteo/vips/enums/VipsForeignCoding", "None");
    assertEqualsNativeEnumValue(env, VIPS_FOREIGN_CODING_LABQ, "com/criteo/vips/enums/VipsForeignCoding", "Labq");
    assertEqualsNativeEnumValue(env, VIPS_FOREIGN_CODING_RAD, "com/criteo/vips/enums/VipsForeignCoding", "Rad");
    assertEqualsNativeEnumValue(env, VIPS_FOREIGN_CODING_ALL, "com/criteo/vips/enums/VipsForeignCoding", "All");
    // VipsForeignDzContainer
    assertEqualsNativeEnumValue(env, VIPS_FOREIGN_DZ_CONTAINER_FS, "com/criteo/vips/enums/VipsForeignDzContainer", "Fs");
    assertEqualsNativeEnumValue(env, VIPS_FOREIGN_DZ_CONTAINER_ZIP, "com/criteo/vips/enums/VipsForeignDzContainer", "Zip");
    assertEqualsNativeEnumValue(env, VIPS_FOREIGN_DZ_CONTAINER_SZI, "com/criteo/vips/enums/VipsForeignDzContainer", "Szi");
    // VipsForeignDzDepth
    assertEqualsNativeEnumValue(env, VIPS_FOREIGN_DZ_DEPTH_ONEPIXEL, "com/criteo/vips/enums/VipsForeignDzDepth", "Onepixel");
    assertEqualsNativeEnumValue(env, VIPS_FOREIGN_DZ_DEPTH_ONETILE, "com/criteo/vips/enums/VipsForeignDzDepth", "Onetile");
    assertEqualsNativeEnumValue(env, VIPS_FOREIGN_DZ_DEPTH_ONE, "com/criteo/vips/enums/VipsForeignDzDepth", "One");
    // VipsForeignDzLayout
    assertEqualsNativeEnumValue(env, VIPS_FOREIGN_DZ_LAYOUT_DZ, "com/criteo/vips/enums/VipsForeignDzLayout", "Dz");
    assertEqualsNativeEnumValue(env, VIPS_FOREIGN_DZ_LAYOUT_ZOOMIFY, "com/criteo/vips/enums/VipsForeignDzLayout", "Zoomify");
    assertEqualsNativeEnumValue(env, VIPS_FOREIGN_DZ_LAYOUT_GOOGLE, "com/criteo/vips/enums/VipsForeignDzLayout", "Google");
    assertEqualsNativeEnumValue(env, VIPS_FOREIGN_DZ_LAYOUT_IIIF, "com/criteo/vips/enums/VipsForeignDzLayout", "Iiif");
    assertEqualsNativeEnumValue(env, VIPS_FOREIGN_DZ_LAYOUT_IIIF3, "com/criteo/vips/enums/VipsForeignDzLayout", "Iiif3");
    // VipsForeignFlags
    assertEqualsNativeEnumValue(env, VIPS_FOREIGN_NONE, "com/criteo/vips/enums/VipsForeignFlags", "ForeignNone");
    assertEqualsNativeEnumValue(env, VIPS_FOREIGN_PARTIAL, "com/criteo/vips/enums/VipsForeignFlags", "ForeignPartial");
    assertEqualsNativeEnumValue(env, VIPS_FOREIGN_BIGENDIAN, "com/criteo/vips/enums/VipsForeignFlags", "ForeignBigendian");
    assertEqualsNativeEnumValue(env, VIPS_FOREIGN_SEQUENTIAL, "com/criteo/vips/enums/VipsForeignFlags", "ForeignSequential");
    assertEqualsNativeEnumValue(env, VIPS_FOREIGN_ALL, "com/criteo/vips/enums/VipsForeignFlags", "ForeignAll");
    // VipsForeignHeifCompression
    assertEqualsNativeEnumValue(env, VIPS_FOREIGN_HEIF_COMPRESSION_HEVC, "com/criteo/vips/enums/VipsForeignHeifCompression", "Hevc");
    assertEqualsNativeEnumValue(env, VIPS_FOREIGN_HEIF_COMPRESSION_AVC, "com/criteo/vips/enums/VipsForeignHeifCompression", "Avc");
    assertEqualsNativeEnumValue(env, VIPS_FOREIGN_HEIF_COMPRESSION_JPEG, "com/criteo/vips/enums/VipsForeignHeifCompression", "Jpeg");
    assertEqualsNativeEnumValue(env, VIPS_FOREIGN_HEIF_COMPRESSION_AV1, "com/criteo/vips/enums/VipsForeignHeifCompression", "Av1");
    // VipsForeignHeifEncoder
    assertEqualsNativeEnumValue(env, VIPS_FOREIGN_HEIF_ENCODER_AUTO, "com/criteo/vips/enums/VipsForeignHeifEncoder", "Auto");
    assertEqualsNativeEnumValue(env, VIPS_FOREIGN_HEIF_ENCODER_AOM, "com/criteo/vips/enums/VipsForeignHeifEncoder", "Aom");
    assertEqualsNativeEnumValue(env, VIPS_FOREIGN_HEIF_ENCODER_RAV1E, "com/criteo/vips/enums/VipsForeignHeifEncoder", "Rav1e");
    assertEqualsNativeEnumValue(env, VIPS_FOREIGN_HEIF_ENCODER_SVT, "com/criteo/vips/enums/VipsForeignHeifEncoder", "Svt");
    assertEqualsNativeEnumValue(env, VIPS_FOREIGN_HEIF_ENCODER_X265, "com/criteo/vips/enums/VipsForeignHeifEncoder", "X265");
    // VipsForeignJpegSubsample
    assertEqualsNativeEnumValue(env, VIPS_FOREIGN_JPEG_SUBSAMPLE_AUTO, "com/criteo/vips/enums/VipsForeignJpegSubsample", "Auto");
    assertEqualsNativeEnumValue(env, VIPS_FOREIGN_JPEG_SUBSAMPLE_ON, "com/criteo/vips/enums/VipsForeignJpegSubsample", "On");
    assertEqualsNativeEnumValue(env, VIPS_FOREIGN_JPEG_SUBSAMPLE_OFF, "com/criteo/vips/enums/VipsForeignJpegSubsample", "Off");
    // VipsForeignKeep
    assertEqualsNativeEnumValue(env, VIPS_FOREIGN_KEEP_NONE, "com/criteo/vips/enums/VipsForeignKeep", "None");
    assertEqualsNativeEnumValue(env, VIPS_FOREIGN_KEEP_EXIF, "com/criteo/vips/enums/VipsForeignKeep", "Exif");
    assertEqualsNativeEnumValue(env, VIPS_FOREIGN_KEEP_XMP, "com/criteo/vips/enums/VipsForeignKeep", "Xmp");
    assertEqualsNativeEnumValue(env, VIPS_FOREIGN_KEEP_IPTC, "com/criteo/vips/enums/VipsForeignKeep", "Iptc");
    assertEqualsNativeEnumValue(env, VIPS_FOREIGN_KEEP_ICC, "com/criteo/vips/enums/VipsForeignKeep", "Icc");
    assertEqualsNativeEnumValue(env, VIPS_FOREIGN_KEEP_OTHER, "com/criteo/vips/enums/VipsForeignKeep", "Other");
    assertEqualsNativeEnumValue(env, VIPS_FOREIGN_KEEP_GAINMAP, "com/criteo/vips/enums/VipsForeignKeep", "Gainmap");
    assertEqualsNativeEnumValue(env, VIPS_FOREIGN_KEEP_ALL, "com/criteo/vips/enums/VipsForeignKeep", "All");
    // VipsForeignPdfPageBox
    assertEqualsNativeEnumValue(env, VIPS_FOREIGN_PDF_PAGE_BOX_MEDIA, "com/criteo/vips/enums/VipsForeignPdfPageBox", "Media");
    assertEqualsNativeEnumValue(env, VIPS_FOREIGN_PDF_PAGE_BOX_CROP, "com/criteo/vips/enums/VipsForeignPdfPageBox", "Crop");
    assertEqualsNativeEnumValue(env, VIPS_FOREIGN_PDF_PAGE_BOX_TRIM, "com/criteo/vips/enums/VipsForeignPdfPageBox", "Trim");
    assertEqualsNativeEnumValue(env, VIPS_FOREIGN_PDF_PAGE_BOX_BLEED, "com/criteo/vips/enums/VipsForeignPdfPageBox", "Bleed");
    assertEqualsNativeEnumValue(env, VIPS_FOREIGN_PDF_PAGE_BOX_ART, "com/criteo/vips/enums/VipsForeignPdfPageBox", "Art");
    // VipsForeignPngFilter
    assertEqualsNativeEnumValue(env, VIPS_FOREIGN_PNG_FILTER_NONE, "com/criteo/vips/enums/VipsForeignPngFilter", "None");
    assertEqualsNativeEnumValue(env, VIPS_FOREIGN_PNG_FILTER_SUB, "com/criteo/vips/enums/VipsForeignPngFilter", "Sub");
    assertEqualsNativeEnumValue(env, VIPS_FOREIGN_PNG_FILTER_UP, "com/criteo/vips/enums/VipsForeignPngFilter", "Up");
    assertEqualsNativeEnumValue(env, VIPS_FOREIGN_PNG_FILTER_AVG, "com/criteo/vips/enums/VipsForeignPngFilter", "Avg");
    assertEqualsNativeEnumValue(env, VIPS_FOREIGN_PNG_FILTER_PAETH, "com/criteo/vips/enums/VipsForeignPngFilter", "Paeth");
    assertEqualsNativeEnumValue(env, VIPS_FOREIGN_PNG_FILTER_ALL, "com/criteo/vips/enums/VipsForeignPngFilter", "All");
    // VipsForeignPpmFormat
    assertEqualsNativeEnumValue(env, VIPS_FOREIGN_PPM_FORMAT_PBM, "com/criteo/vips/enums/VipsForeignPpmFormat", "Pbm");
    assertEqualsNativeEnumValue(env, VIPS_FOREIGN_PPM_FORMAT_PGM, "com/criteo/vips/enums/VipsForeignPpmFormat", "Pgm");
    assertEqualsNativeEnumValue(env, VIPS_FOREIGN_PPM_FORMAT_PPM, "com/criteo/vips/enums/VipsForeignPpmFormat", "Ppm");
    assertEqualsNativeEnumValue(env, VIPS_FOREIGN_PPM_FORMAT_PFM, "com/criteo/vips/enums/VipsForeignPpmFormat", "Pfm");
    assertEqualsNativeEnumValue(env, VIPS_FOREIGN_PPM_FORMAT_PNM, "com/criteo/vips/enums/VipsForeignPpmFormat", "Pnm");
    // VipsForeignSaveable
    assertEqualsNativeEnumValue(env, VIPS_FOREIGN_SAVEABLE_ANY, "com/criteo/vips/enums/VipsForeignSaveable", "Any");
    assertEqualsNativeEnumValue(env, VIPS_FOREIGN_SAVEABLE_MONO, "com/criteo/vips/enums/VipsForeignSaveable", "Mono");
    assertEqualsNativeEnumValue(env, VIPS_FOREIGN_SAVEABLE_RGB, "com/criteo/vips/enums/VipsForeignSaveable", "Rgb");
    assertEqualsNativeEnumValue(env, VIPS_FOREIGN_SAVEABLE_CMYK, "com/criteo/vips/enums/VipsForeignSaveable", "Cmyk");
    assertEqualsNativeEnumValue(env, VIPS_FOREIGN_SAVEABLE_ALPHA, "com/criteo/vips/enums/VipsForeignSaveable", "Alpha");
    assertEqualsNativeEnumValue(env, VIPS_FOREIGN_SAVEABLE_ALL, "com/criteo/vips/enums/VipsForeignSaveable", "All");
    // VipsForeignSubsample
    assertEqualsNativeEnumValue(env, VIPS_FOREIGN_SUBSAMPLE_AUTO, "com/criteo/vips/enums/VipsForeignSubsample", "Auto");
    assertEqualsNativeEnumValue(env, VIPS_FOREIGN_SUBSAMPLE_ON, "com/criteo/vips/enums/VipsForeignSubsample", "On");
    assertEqualsNativeEnumValue(env, VIPS_FOREIGN_SUBSAMPLE_OFF, "com/criteo/vips/enums/VipsForeignSubsample", "Off");
    // VipsForeignTiffCompression
    assertEqualsNativeEnumValue(env, VIPS_FOREIGN_TIFF_COMPRESSION_NONE, "com/criteo/vips/enums/VipsForeignTiffCompression", "None");
    assertEqualsNativeEnumValue(env, VIPS_FOREIGN_TIFF_COMPRESSION_JPEG, "com/criteo/vips/enums/VipsForeignTiffCompression", "Jpeg");
    assertEqualsNativeEnumValue(env, VIPS_FOREIGN_TIFF_COMPRESSION_DEFLATE, "com/criteo/vips/enums/VipsForeignTiffCompression", "Deflate");
    assertEqualsNativeEnumValue(env, VIPS_FOREIGN_TIFF_COMPRESSION_PACKBITS, "com/criteo/vips/enums/VipsForeignTiffCompression", "Packbits");
    assertEqualsNativeEnumValue(env, VIPS_FOREIGN_TIFF_COMPRESSION_CCITTFAX4, "com/criteo/vips/enums/VipsForeignTiffCompression", "Ccittfax4");
    assertEqualsNativeEnumValue(env, VIPS_FOREIGN_TIFF_COMPRESSION_LZW, "com/criteo/vips/enums/VipsForeignTiffCompression", "Lzw");
    assertEqualsNativeEnumValue(env, VIPS_FOREIGN_TIFF_COMPRESSION_WEBP, "com/criteo/vips/enums/VipsForeignTiffCompression", "Webp");
    assertEqualsNativeEnumValue(env, VIPS_FOREIGN_TIFF_COMPRESSION_ZSTD, "com/criteo/vips/enums/VipsForeignTiffCompression", "Zstd");
    assertEqualsNativeEnumValue(env, VIPS_FOREIGN_TIFF_COMPRESSION_JP2K, "com/criteo/vips/enums/VipsForeignTiffCompression", "Jp2k");
    // VipsForeignTiffPredictor
    assertEqualsNativeEnumValue(env, VIPS_FOREIGN_TIFF_PREDICTOR_NONE, "com/criteo/vips/enums/VipsForeignTiffPredictor", "None");
    assertEqualsNativeEnumValue(env, VIPS_FOREIGN_TIFF_PREDICTOR_HORIZONTAL, "com/criteo/vips/enums/VipsForeignTiffPredictor", "Horizontal");
    assertEqualsNativeEnumValue(env, VIPS_FOREIGN_TIFF_PREDICTOR_FLOAT, "com/criteo/vips/enums/VipsForeignTiffPredictor", "Float");
    // VipsForeignTiffResunit
    assertEqualsNativeEnumValue(env, VIPS_FOREIGN_TIFF_RESUNIT_CM, "com/criteo/vips/enums/VipsForeignTiffResunit", "Cm");
    assertEqualsNativeEnumValue(env, VIPS_FOREIGN_TIFF_RESUNIT_INCH, "com/criteo/vips/enums/VipsForeignTiffResunit", "Inch");
    // VipsForeignWebpPreset
    assertEqualsNativeEnumValue(env, VIPS_FOREIGN_WEBP_PRESET_DEFAULT, "com/criteo/vips/enums/VipsForeignWebpPreset", "Default");
    assertEqualsNativeEnumValue(env, VIPS_FOREIGN_WEBP_PRESET_PICTURE, "com/criteo/vips/enums/VipsForeignWebpPreset", "Picture");
    assertEqualsNativeEnumValue(env, VIPS_FOREIGN_WEBP_PRESET_PHOTO, "com/criteo/vips/enums/VipsForeignWebpPreset", "Photo");
    assertEqualsNativeEnumValue(env, VIPS_FOREIGN_WEBP_PRESET_DRAWING, "com/criteo/vips/enums/VipsForeignWebpPreset", "Drawing");
    assertEqualsNativeEnumValue(env, VIPS_FOREIGN_WEBP_PRESET_ICON, "com/criteo/vips/enums/VipsForeignWebpPreset", "Icon");
    assertEqualsNativeEnumValue(env, VIPS_FOREIGN_WEBP_PRESET_TEXT, "com/criteo/vips/enums/VipsForeignWebpPreset", "Text");
    // VipsImageType
    assertEqualsNativeEnumValue(env, VIPS_IMAGE_ERROR, "com/criteo/vips/enums/VipsImageType", "ImageError");
    assertEqualsNativeEnumValue(env, VIPS_IMAGE_NONE, "com/criteo/vips/enums/VipsImageType", "ImageNone");
    assertEqualsNativeEnumValue(env, VIPS_IMAGE_SETBUF, "com/criteo/vips/enums/VipsImageType", "ImageSetbuf");
    assertEqualsNativeEnumValue(env, VIPS_IMAGE_SETBUF_FOREIGN, "com/criteo/vips/enums/VipsImageType", "ImageSetbufForeign");
    assertEqualsNativeEnumValue(env, VIPS_IMAGE_OPENIN, "com/criteo/vips/enums/VipsImageType", "ImageOpenin");
    assertEqualsNativeEnumValue(env, VIPS_IMAGE_MMAPIN, "com/criteo/vips/enums/VipsImageType", "ImageMmapin");
    assertEqualsNativeEnumValue(env, VIPS_IMAGE_MMAPINRW, "com/criteo/vips/enums/VipsImageType", "ImageMmapinrw");
    assertEqualsNativeEnumValue(env, VIPS_IMAGE_OPENOUT, "com/criteo/vips/enums/VipsImageType", "ImageOpenout");
    assertEqualsNativeEnumValue(env, VIPS_IMAGE_PARTIAL, "com/criteo/vips/enums/VipsImageType", "ImagePartial");
    // VipsIntent
    assertEqualsNativeEnumValue(env, VIPS_INTENT_PERCEPTUAL, "com/criteo/vips/enums/VipsIntent", "Perceptual");
    assertEqualsNativeEnumValue(env, VIPS_INTENT_RELATIVE, "com/criteo/vips/enums/VipsIntent", "Relative");
    assertEqualsNativeEnumValue(env, VIPS_INTENT_SATURATION, "com/criteo/vips/enums/VipsIntent", "Saturation");
    assertEqualsNativeEnumValue(env, VIPS_INTENT_ABSOLUTE, "com/criteo/vips/enums/VipsIntent", "Absolute");
    assertEqualsNativeEnumValue(env, VIPS_INTENT_AUTO, "com/criteo/vips/enums/VipsIntent", "Auto");
    // VipsInteresting
    assertEqualsNativeEnumValue(env, VIPS_INTERESTING_NONE, "com/criteo/vips/enums/VipsInteresting", "None");
    assertEqualsNativeEnumValue(env, VIPS_INTERESTING_CENTRE, "com/criteo/vips/enums/VipsInteresting", "Centre");
    assertEqualsNativeEnumValue(env, VIPS_INTERESTING_ENTROPY, "com/criteo/vips/enums/VipsInteresting", "Entropy");
    assertEqualsNativeEnumValue(env, VIPS_INTERESTING_ATTENTION, "com/criteo/vips/enums/VipsInteresting", "Attention");
    assertEqualsNativeEnumValue(env, VIPS_INTERESTING_LOW, "com/criteo/vips/enums/VipsInteresting", "Low");
    assertEqualsNativeEnumValue(env, VIPS_INTERESTING_HIGH, "com/criteo/vips/enums/VipsInteresting", "High");
    assertEqualsNativeEnumValue(env, VIPS_INTERESTING_ALL, "com/criteo/vips/enums/VipsInteresting", "All");
    // VipsInterpretation
    assertEqualsNativeEnumValue(env, VIPS_INTERPRETATION_ERROR, "com/criteo/vips/enums/VipsInterpretation", "Error");
    assertEqualsNativeEnumValue(env, VIPS_INTERPRETATION_MULTIBAND, "com/criteo/vips/enums/VipsInterpretation", "Multiband");
    assertEqualsNativeEnumValue(env, VIPS_INTERPRETATION_B_W, "com/criteo/vips/enums/VipsInterpretation", "BW");
    assertEqualsNativeEnumValue(env, VIPS_INTERPRETATION_HISTOGRAM, "com/criteo/vips/enums/VipsInterpretation", "Histogram");
    assertEqualsNativeEnumValue(env, VIPS_INTERPRETATION_XYZ, "com/criteo/vips/enums/VipsInterpretation", "Xyz");
    assertEqualsNativeEnumValue(env, VIPS_INTERPRETATION_LAB, "com/criteo/vips/enums/VipsInterpretation", "Lab");
    assertEqualsNativeEnumValue(env, VIPS_INTERPRETATION_CMYK, "com/criteo/vips/enums/VipsInterpretation", "Cmyk");
    assertEqualsNativeEnumValue(env, VIPS_INTERPRETATION_LABQ, "com/criteo/vips/enums/VipsInterpretation", "Labq");
    assertEqualsNativeEnumValue(env, VIPS_INTERPRETATION_RGB, "com/criteo/vips/enums/VipsInterpretation", "Rgb");
    assertEqualsNativeEnumValue(env, VIPS_INTERPRETATION_CMC, "com/criteo/vips/enums/VipsInterpretation", "Cmc");
    assertEqualsNativeEnumValue(env, VIPS_INTERPRETATION_LCH, "com/criteo/vips/enums/VipsInterpretation", "Lch");
    assertEqualsNativeEnumValue(env, VIPS_INTERPRETATION_LABS, "com/criteo/vips/enums/VipsInterpretation", "Labs");
    assertEqualsNativeEnumValue(env, VIPS_INTERPRETATION_sRGB, "com/criteo/vips/enums/VipsInterpretation", "Srgb");
    assertEqualsNativeEnumValue(env, VIPS_INTERPRETATION_YXY, "com/criteo/vips/enums/VipsInterpretation", "Yxy");
    assertEqualsNativeEnumValue(env, VIPS_INTERPRETATION_FOURIER, "com/criteo/vips/enums/VipsInterpretation", "Fourier");
    assertEqualsNativeEnumValue(env, VIPS_INTERPRETATION_RGB16, "com/criteo/vips/enums/VipsInterpretation", "Rgb16");
    assertEqualsNativeEnumValue(env, VIPS_INTERPRETATION_GREY16, "com/criteo/vips/enums/VipsInterpretation", "Grey16");
    assertEqualsNativeEnumValue(env, VIPS_INTERPRETATION_MATRIX, "com/criteo/vips/enums/VipsInterpretation", "Matrix");
    assertEqualsNativeEnumValue(env, VIPS_INTERPRETATION_scRGB, "com/criteo/vips/enums/VipsInterpretation", "Scrgb");
    assertEqualsNativeEnumValue(env, VIPS_INTERPRETATION_HSV, "com/criteo/vips/enums/VipsInterpretation", "Hsv");
    assertEqualsNativeEnumValue(env, VIPS_INTERPRETATION_OKLAB, "com/criteo/vips/enums/VipsInterpretation", "Oklab");
    assertEqualsNativeEnumValue(env, VIPS_INTERPRETATION_OKLCH, "com/criteo/vips/enums/VipsInterpretation", "Oklch");
    // VipsKernel
    assertEqualsNativeEnumValue(env, VIPS_KERNEL_NEAREST, "com/criteo/vips/enums/VipsKernel", "Nearest");
    assertEqualsNativeEnumValue(env, VIPS_KERNEL_LINEAR, "com/criteo/vips/enums/VipsKernel", "Linear");
    assertEqualsNativeEnumValue(env, VIPS_KERNEL_CUBIC, "com/criteo/vips/enums/VipsKernel", "Cubic");
    assertEqualsNativeEnumValue(env, VIPS_KERNEL_MITCHELL, "com/criteo/vips/enums/VipsKernel", "Mitchell");
    assertEqualsNativeEnumValue(env, VIPS_KERNEL_LANCZOS2, "com/criteo/vips/enums/VipsKernel", "Lanczos2");
    assertEqualsNativeEnumValue(env, VIPS_KERNEL_LANCZOS3, "com/criteo/vips/enums/VipsKernel", "Lanczos3");
    assertEqualsNativeEnumValue(env, VIPS_KERNEL_MKS2013, "com/criteo/vips/enums/VipsKernel", "Mks2013");
    assertEqualsNativeEnumValue(env, VIPS_KERNEL_MKS2021, "com/criteo/vips/enums/VipsKernel", "Mks2021");
    // VipsOperationBoolean
    assertEqualsNativeEnumValue(env, VIPS_OPERATION_BOOLEAN_AND, "com/criteo/vips/enums/VipsOperationBoolean", "And");
    assertEqualsNativeEnumValue(env, VIPS_OPERATION_BOOLEAN_OR, "com/criteo/vips/enums/VipsOperationBoolean", "Or");
    assertEqualsNativeEnumValue(env, VIPS_OPERATION_BOOLEAN_EOR, "com/criteo/vips/enums/VipsOperationBoolean", "Eor");
    assertEqualsNativeEnumValue(env, VIPS_OPERATION_BOOLEAN_LSHIFT, "com/criteo/vips/enums/VipsOperationBoolean", "Lshift");
    assertEqualsNativeEnumValue(env, VIPS_OPERATION_BOOLEAN_RSHIFT, "com/criteo/vips/enums/VipsOperationBoolean", "Rshift");
    // VipsOperationComplex
    assertEqualsNativeEnumValue(env, VIPS_OPERATION_COMPLEX_POLAR, "com/criteo/vips/enums/VipsOperationComplex", "Polar");
    assertEqualsNativeEnumValue(env, VIPS_OPERATION_COMPLEX_RECT, "com/criteo/vips/enums/VipsOperationComplex", "Rect");
    assertEqualsNativeEnumValue(env, VIPS_OPERATION_COMPLEX_CONJ, "com/criteo/vips/enums/VipsOperationComplex", "Conj");
    // VipsOperationComplex2
    assertEqualsNativeEnumValue(env, VIPS_OPERATION_COMPLEX2_CROSS_PHASE, "com/criteo/vips/enums/VipsOperationComplex2", "CrossPhase");
    // VipsOperationComplexget
    assertEqualsNativeEnumValue(env, VIPS_OPERATION_COMPLEXGET_REAL, "com/criteo/vips/enums/VipsOperationComplexget", "Real");
    assertEqualsNativeEnumValue(env, VIPS_OPERATION_COMPLEXGET_IMAG, "com/criteo/vips/enums/VipsOperationComplexget", "Imag");
    // VipsOperationFlags
    assertEqualsNativeEnumValue(env, VIPS_OPERATION_NONE, "com/criteo/vips/enums/VipsOperationFlags", "OperationNone");
    assertEqualsNativeEnumValue(env, VIPS_OPERATION_SEQUENTIAL, "com/criteo/vips/enums/VipsOperationFlags", "OperationSequential");
    assertEqualsNativeEnumValue(env, VIPS_OPERATION_SEQUENTIAL_UNBUFFERED, "com/criteo/vips/enums/VipsOperationFlags", "OperationSequentialUnbuffered");
    assertEqualsNativeEnumValue(env, VIPS_OPERATION_NOCACHE, "com/criteo/vips/enums/VipsOperationFlags", "OperationNocache");
    assertEqualsNativeEnumValue(env, VIPS_OPERATION_DEPRECATED, "com/criteo/vips/enums/VipsOperationFlags", "OperationDeprecated");
    assertEqualsNativeEnumValue(env, VIPS_OPERATION_UNTRUSTED, "com/criteo/vips/enums/VipsOperationFlags", "OperationUntrusted");
    assertEqualsNativeEnumValue(env, VIPS_OPERATION_BLOCKED, "com/criteo/vips/enums/VipsOperationFlags", "OperationBlocked");
    assertEqualsNativeEnumValue(env, VIPS_OPERATION_REVALIDATE, "com/criteo/vips/enums/VipsOperationFlags", "OperationRevalidate");
    // VipsOperationMath
    assertEqualsNativeEnumValue(env, VIPS_OPERATION_MATH_SIN, "com/criteo/vips/enums/VipsOperationMath", "Sin");
    assertEqualsNativeEnumValue(env, VIPS_OPERATION_MATH_COS, "com/criteo/vips/enums/VipsOperationMath", "Cos");
    assertEqualsNativeEnumValue(env, VIPS_OPERATION_MATH_TAN, "com/criteo/vips/enums/VipsOperationMath", "Tan");
    assertEqualsNativeEnumValue(env, VIPS_OPERATION_MATH_ASIN, "com/criteo/vips/enums/VipsOperationMath", "Asin");
    assertEqualsNativeEnumValue(env, VIPS_OPERATION_MATH_ACOS, "com/criteo/vips/enums/VipsOperationMath", "Acos");
    assertEqualsNativeEnumValue(env, VIPS_OPERATION_MATH_ATAN, "com/criteo/vips/enums/VipsOperationMath", "Atan");
    assertEqualsNativeEnumValue(env, VIPS_OPERATION_MATH_LOG, "com/criteo/vips/enums/VipsOperationMath", "Log");
    assertEqualsNativeEnumValue(env, VIPS_OPERATION_MATH_LOG10, "com/criteo/vips/enums/VipsOperationMath", "Log10");
    assertEqualsNativeEnumValue(env, VIPS_OPERATION_MATH_EXP, "com/criteo/vips/enums/VipsOperationMath", "Exp");
    assertEqualsNativeEnumValue(env, VIPS_OPERATION_MATH_EXP10, "com/criteo/vips/enums/VipsOperationMath", "Exp10");
    assertEqualsNativeEnumValue(env, VIPS_OPERATION_MATH_SINH, "com/criteo/vips/enums/VipsOperationMath", "Sinh");
    assertEqualsNativeEnumValue(env, VIPS_OPERATION_MATH_COSH, "com/criteo/vips/enums/VipsOperationMath", "Cosh");
    assertEqualsNativeEnumValue(env, VIPS_OPERATION_MATH_TANH, "com/criteo/vips/enums/VipsOperationMath", "Tanh");
    assertEqualsNativeEnumValue(env, VIPS_OPERATION_MATH_ASINH, "com/criteo/vips/enums/VipsOperationMath", "Asinh");
    assertEqualsNativeEnumValue(env, VIPS_OPERATION_MATH_ACOSH, "com/criteo/vips/enums/VipsOperationMath", "Acosh");
    assertEqualsNativeEnumValue(env, VIPS_OPERATION_MATH_ATANH, "com/criteo/vips/enums/VipsOperationMath", "Atanh");
    // VipsOperationMath2
    assertEqualsNativeEnumValue(env, VIPS_OPERATION_MATH2_POW, "com/criteo/vips/enums/VipsOperationMath2", "Pow");
    assertEqualsNativeEnumValue(env, VIPS_OPERATION_MATH2_WOP, "com/criteo/vips/enums/VipsOperationMath2", "Wop");
    assertEqualsNativeEnumValue(env, VIPS_OPERATION_MATH2_ATAN2, "com/criteo/vips/enums/VipsOperationMath2", "Atan2");
    // VipsOperationMorphology
    assertEqualsNativeEnumValue(env, VIPS_OPERATION_MORPHOLOGY_ERODE, "com/criteo/vips/enums/VipsOperationMorphology", "Erode");
    assertEqualsNativeEnumValue(env, VIPS_OPERATION_MORPHOLOGY_DILATE, "com/criteo/vips/enums/VipsOperationMorphology", "Dilate");
    // VipsOperationRelational
    assertEqualsNativeEnumValue(env, VIPS_OPERATION_RELATIONAL_EQUAL, "com/criteo/vips/enums/VipsOperationRelational", "Equal");
    assertEqualsNativeEnumValue(env, VIPS_OPERATION_RELATIONAL_NOTEQ, "com/criteo/vips/enums/VipsOperationRelational", "Noteq");
    assertEqualsNativeEnumValue(env, VIPS_OPERATION_RELATIONAL_LESS, "com/criteo/vips/enums/VipsOperationRelational", "Less");
    assertEqualsNativeEnumValue(env, VIPS_OPERATION_RELATIONAL_LESSEQ, "com/criteo/vips/enums/VipsOperationRelational", "Lesseq");
    assertEqualsNativeEnumValue(env, VIPS_OPERATION_RELATIONAL_MORE, "com/criteo/vips/enums/VipsOperationRelational", "More");
    assertEqualsNativeEnumValue(env, VIPS_OPERATION_RELATIONAL_MOREEQ, "com/criteo/vips/enums/VipsOperationRelational", "Moreeq");
    // VipsOperationRound
    assertEqualsNativeEnumValue(env, VIPS_OPERATION_ROUND_RINT, "com/criteo/vips/enums/VipsOperationRound", "Rint");
    assertEqualsNativeEnumValue(env, VIPS_OPERATION_ROUND_CEIL, "com/criteo/vips/enums/VipsOperationRound", "Ceil");
    assertEqualsNativeEnumValue(env, VIPS_OPERATION_ROUND_FLOOR, "com/criteo/vips/enums/VipsOperationRound", "Floor");
    // VipsPCS
    assertEqualsNativeEnumValue(env, VIPS_PCS_LAB, "com/criteo/vips/enums/VipsPCS", "PcsLab");
    assertEqualsNativeEnumValue(env, VIPS_PCS_XYZ, "com/criteo/vips/enums/VipsPCS", "PcsXyz");
    // VipsPrecision
    assertEqualsNativeEnumValue(env, VIPS_PRECISION_INTEGER, "com/criteo/vips/enums/VipsPrecision", "Integer");
    assertEqualsNativeEnumValue(env, VIPS_PRECISION_FLOAT, "com/criteo/vips/enums/VipsPrecision", "Float");
    assertEqualsNativeEnumValue(env, VIPS_PRECISION_APPROXIMATE, "com/criteo/vips/enums/VipsPrecision", "Approximate");
    // VipsRegionShrink
    assertEqualsNativeEnumValue(env, VIPS_REGION_SHRINK_MEAN, "com/criteo/vips/enums/VipsRegionShrink", "Mean");
    assertEqualsNativeEnumValue(env, VIPS_REGION_SHRINK_MEDIAN, "com/criteo/vips/enums/VipsRegionShrink", "Median");
    assertEqualsNativeEnumValue(env, VIPS_REGION_SHRINK_MODE, "com/criteo/vips/enums/VipsRegionShrink", "Mode");
    assertEqualsNativeEnumValue(env, VIPS_REGION_SHRINK_MAX, "com/criteo/vips/enums/VipsRegionShrink", "Max");
    assertEqualsNativeEnumValue(env, VIPS_REGION_SHRINK_MIN, "com/criteo/vips/enums/VipsRegionShrink", "Min");
    assertEqualsNativeEnumValue(env, VIPS_REGION_SHRINK_NEAREST, "com/criteo/vips/enums/VipsRegionShrink", "Nearest");
    // VipsSdfShape
    assertEqualsNativeEnumValue(env, VIPS_SDF_SHAPE_CIRCLE, "com/criteo/vips/enums/VipsSdfShape", "Circle");
    assertEqualsNativeEnumValue(env, VIPS_SDF_SHAPE_BOX, "com/criteo/vips/enums/VipsSdfShape", "Box");
    assertEqualsNativeEnumValue(env, VIPS_SDF_SHAPE_ROUNDED_BOX, "com/criteo/vips/enums/VipsSdfShape", "RoundedBox");
    assertEqualsNativeEnumValue(env, VIPS_SDF_SHAPE_LINE, "com/criteo/vips/enums/VipsSdfShape", "Line");
    // VipsSize
    assertEqualsNativeEnumValue(env, VIPS_SIZE_BOTH, "com/criteo/vips/enums/VipsSize", "Both");
    assertEqualsNativeEnumValue(env, VIPS_SIZE_UP, "com/criteo/vips/enums/VipsSize", "Up");
    assertEqualsNativeEnumValue(env, VIPS_SIZE_DOWN, "com/criteo/vips/enums/VipsSize", "Down");
    assertEqualsNativeEnumValue(env, VIPS_SIZE_FORCE, "com/criteo/vips/enums/VipsSize", "Force");
    // VipsTextWrap
    assertEqualsNativeEnumValue(env, VIPS_TEXT_WRAP_WORD, "com/criteo/vips/enums/VipsTextWrap", "Word");
    assertEqualsNativeEnumValue(env, VIPS_TEXT_WRAP_CHAR, "com/criteo/vips/enums/VipsTextWrap", "Char");
    assertEqualsNativeEnumValue(env, VIPS_TEXT_WRAP_WORD_CHAR, "com/criteo/vips/enums/VipsTextWrap", "WordChar");
    assertEqualsNativeEnumValue(env, VIPS_TEXT_WRAP_NONE, "com/criteo/vips/enums/VipsTextWrap", "None");
    // VipsToken
    assertEqualsNativeEnumValue(env, VIPS_TOKEN_LEFT, "com/criteo/vips/enums/VipsToken", "Left");
    assertEqualsNativeEnumValue(env, VIPS_TOKEN_RIGHT, "com/criteo/vips/enums/VipsToken", "Right");
    assertEqualsNativeEnumValue(env, VIPS_TOKEN_STRING, "com/criteo/vips/enums/VipsToken", "String");
    assertEqualsNativeEnumValue(env, VIPS_TOKEN_EQUALS, "com/criteo/vips/enums/VipsToken", "Equals");
    assertEqualsNativeEnumValue(env, VIPS_TOKEN_COMMA, "com/criteo/vips/enums/VipsToken", "Comma");

}
