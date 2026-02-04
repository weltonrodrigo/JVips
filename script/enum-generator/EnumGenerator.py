import os
import glob
import re
import argparse
import textwrap
import xml.etree.ElementTree as ET
from string import Template
from itertools import takewhile

JAVA_ENUM_TEMPLATE = "template/Enum.java"
C_ENUM_TEST_TEMPLATE = "template/VipsEnumTest.c"

# GIR XML namespaces
GIR_NS = {
    'core': 'http://www.gtk.org/introspection/core/1.0',
    'c': 'http://www.gtk.org/introspection/c/1.0',
    'glib': 'http://www.gtk.org/introspection/glib/1.0',
}

# Register namespaces for ElementTree
for prefix, uri in GIR_NS.items():
    ET.register_namespace(prefix, uri)


def ns(tag, namespace='core'):
    """Create a namespaced tag name for ElementTree."""
    return '{%s}%s' % (GIR_NS[namespace], tag)


def parse_gir_file(gir_path):
    """Parse a GIR file and extract all enumerations and bitfields."""
    tree = ET.parse(gir_path)
    root = tree.getroot()

    enums = []

    # Find all enumeration and bitfield elements
    for enum_elem in root.iter():
        if enum_elem.tag in (ns('enumeration'), ns('bitfield')):
            enum_data = parse_enum_element(enum_elem)
            if enum_data:
                enums.append(enum_data)

    return enums


def parse_enum_element(enum_elem):
    """Parse a single enumeration or bitfield element."""
    name = enum_elem.get('name')
    c_type = enum_elem.get(ns('type', 'c'))

    # Get enum-level documentation
    doc_elem = enum_elem.find(ns('doc'))
    enum_doc = doc_elem.text.strip() if doc_elem is not None and doc_elem.text else None

    members = []
    for member_elem in enum_elem.findall(ns('member')):
        member_name = member_elem.get('name')
        member_value = member_elem.get('value')
        c_identifier = member_elem.get(ns('identifier', 'c'))

        # Get member-level documentation
        member_doc_elem = member_elem.find(ns('doc'))
        member_doc = member_doc_elem.text.strip() if member_doc_elem is not None and member_doc_elem.text else None

        members.append({
            'name': member_name,
            'value': int(member_value),
            'c_identifier': c_identifier,
            'doc': member_doc,
        })

    if not members:
        return None

    return {
        'name': name,
        'c_type': c_type,
        'doc': enum_doc,
        'members': members,
    }


def to_java_name(member_name):
    """Convert snake_case member name to PascalCase Java name.

    Examples:
        'random' -> 'Random'
        'sequential_unbuffered' -> 'SequentialUnbuffered'
    """
    return member_name.title().replace('_', '')


def c_identifier_to_java_name(c_identifier):
    """Convert C identifier to PascalCase Java name.

    Takes a C identifier like VIPS_FORMAT_UCHAR, strips the VIPS_ prefix,
    and converts to PascalCase.

    Examples:
        'VIPS_FORMAT_UCHAR' -> 'FormatUchar'
        'VIPS_ACCESS_RANDOM' -> 'AccessRandom'
        'VIPS_COMPASS_DIRECTION_CENTRE' -> 'CompassDirectionCentre'
    """
    # Strip VIPS_ prefix
    if c_identifier.startswith('VIPS_'):
        c_identifier = c_identifier[5:]

    # Convert SCREAMING_SNAKE_CASE to PascalCase
    parts = c_identifier.lower().split('_')
    return ''.join(part.capitalize() for part in parts)


def format_javadoc(doc_text, indent=''):
    """Format documentation text as a Javadoc comment.

    Args:
        doc_text: The raw documentation text from GIR
        indent: Whitespace prefix for each line

    Returns:
        Formatted Javadoc comment string, or empty string if no doc
    """
    if not doc_text:
        return ''

    # Clean up the documentation text
    doc_text = doc_text.strip()
    # Replace multiple whitespace/newlines with single space
    doc_text = re.sub(r'\s+', ' ', doc_text)

    # Wrap text to reasonable line length
    wrapped = textwrap.wrap(doc_text, width=70)

    if len(wrapped) == 1:
        return f'{indent}/** {wrapped[0]} */\n'
    else:
        lines = [f'{indent}/**']
        for line in wrapped:
            lines.append(f'{indent} * {line}')
        lines.append(f'{indent} */')
        return '\n'.join(lines) + '\n'


def format_member_javadoc(doc_text):
    """Format member documentation as a Javadoc comment with 4-space indent."""
    return format_javadoc(doc_text, indent='    ')


def format_enum_javadoc(doc_text):
    """Format enum-level documentation as a Javadoc comment."""
    return format_javadoc(doc_text, indent='')


def lcp(*s):
    """Longest common prefix of strings."""
    return ''.join(a for a, b in takewhile(lambda x: x[0] == x[1], zip(min(s), max(s))))


def generate_enum_files(enums, enum_output_dir, test_output_dir, license_comment):
    """Generate Java enum files and C test file from parsed enum data."""
    tests = []

    with open(JAVA_ENUM_TEMPLATE, 'r', encoding='utf-8') as infile:
        enum_template = infile.read()

    for enum_data in sorted(enums, key=lambda e: e['name']):
        gir_name = enum_data['name']
        # Add "Vips" prefix to match legacy naming convention
        name = 'Vips' + gir_name
        members = enum_data['members']
        enum_doc = enum_data['doc']

        values = []
        tests.append(f'    // {name}\n')

        for member in members:
            c_identifier = member['c_identifier']
            value = member['value']
            member_doc = member['doc']

            # Derive Java name from C identifier (e.g., VIPS_FORMAT_UCHAR -> FormatUchar)
            java_name = c_identifier_to_java_name(c_identifier)

            # Remove the enum type name prefix if present
            # e.g., for Access enum: 'AccessRandom' -> 'Random'
            # but for BandFormat enum: 'FormatUchar' stays as 'FormatUchar' (no 'BandFormat' prefix)
            if java_name.startswith(gir_name):
                java_name = java_name[len(gir_name):]

            # Ensure java_name is not empty (can happen if member name equals enum name)
            if not java_name:
                java_name = c_identifier_to_java_name(c_identifier)

            # Add test assertion
            tests.append(
                f'    assertEqualsNativeEnumValue(env, {c_identifier}, "com/criteo/vips/enums/{name}", "{java_name}");\n'
            )

            # Build the enum value with optional Javadoc
            if member_doc:
                javadoc = format_member_javadoc(member_doc)
                value_str = f'{javadoc}    {java_name}({value})'
            else:
                value_str = f'    {java_name}({value})'

            values.append(value_str)

        # Generate enum-level Javadoc
        enum_javadoc = format_enum_javadoc(enum_doc).rstrip('\n') if enum_doc else ''

        # Generate the enum file
        values_str = ',\n'.join(values)
        src = Template(enum_template)
        src = src.substitute({
            'license': license_comment,
            'name': name,
            'values': values_str,
            'enum_javadoc': enum_javadoc,
        })

        with open(f'{enum_output_dir}/{name}.java', 'w', encoding='utf-8') as outfile:
            outfile.write(src)

    # Generate the C test file
    with open(C_ENUM_TEST_TEMPLATE, 'r', encoding='utf-8') as infile:
        test_template = infile.read()

    tests_str = ''.join(tests)
    src = Template(test_template)
    src = src.substitute({'license': license_comment, 'tests': tests_str})

    with open(f'{test_output_dir}/{os.path.basename(C_ENUM_TEST_TEMPLATE)}', 'w', encoding='utf-8') as outfile:
        outfile.write(src)


def main():
    parser = argparse.ArgumentParser(
        description='Generate Java enumerations from libvips GIR file.')
    parser.add_argument('--gir', type=str, required=True,
                        help='Path to Vips-8.0.gir file')

    args = parser.parse_args()
    gir_path = args.gir

    if not os.path.isfile('EnumGenerator.py'):
        raise Exception(
            "Script must run from the script/enum-generator directory")

    if not os.path.isfile(gir_path):
        raise FileNotFoundError(f"GIR file not found: {gir_path}")

    enum_output_dir = os.path.join(
        os.getcwd(), '../../src/main/java/com/criteo/vips/enums')
    test_output_dir = os.path.join(os.getcwd(), '../../src/test/c')

    # Clean up existing generated files (except VipsImageFormat.java which is manually maintained)
    for java_file in glob.glob(f'{enum_output_dir}/*.java'):
        if java_file.endswith("VipsImageFormat.java"):
            continue
        os.remove(java_file)
    for c_file in glob.glob(f'{test_output_dir}/VipsEnumTest.c'):
        os.remove(c_file)

    with open(os.path.join(os.getcwd(), 'LICENSE'), 'r', encoding='utf-8') as infile:
        license_comment = infile.read()

    if not os.path.exists(enum_output_dir):
        os.makedirs(enum_output_dir)
    if not os.path.exists(test_output_dir):
        os.makedirs(test_output_dir)

    print(f"Parsing GIR file: {gir_path}")
    enums = parse_gir_file(gir_path)
    print(f"Found {len(enums)} enumerations")

    generate_enum_files(enums, enum_output_dir, test_output_dir, license_comment)
    print(f"Generated enum files in {enum_output_dir}")
    print(f"Generated test file in {test_output_dir}")


if __name__ == '__main__':
    main()
