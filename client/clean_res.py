#!/usr/bin/env python

import os
import re
import shutil
import sys

"""ImageSweep.py: Deletes unnecessary image drawables.."""

__author__ = "Josh Ruesch"
__copyright__ = "Copyright 2014, Instructure Inc"

'''clean_res.py: Delete unnecessary resources'''

__refactor__ = "liqiang"


# Global variables.
res_types = ('anim', 'animator', 'color', 'xml')
used_strings = set()
used_colors = set()
used_drawable_files = set()
used_layout_files = set()
used_res_files = set()
files_deleted = 0
kilo_bytes_deleted = 0


def isIgnoredRoot(directory):
    return (
        directory.startswith('./.git') or
        directory.startswith('./.gradle') or
        directory.startswith('./.idea') or
        directory.startswith('./build') or
        directory.startswith('./code-style') or
        directory.startswith('./gradle') or
        directory.startswith('./app/build'))


def isResourceRoot(directory):
    return (  # TODO: resource other than drawable
        (os.path.exists(directory + "/layout")) or
        (os.path.exists(directory + "/layout-lock")) or
        (os.path.exists(directory + "/menu")) or
        (os.path.exists(directory + "/drawable")) or
        (os.path.exists(directory + "/drawable-nodpi")) or
        (os.path.exists(directory + "/drawable-v21")) or
        (os.path.exists(directory + "/drawable-ldpi")) or
        (os.path.exists(directory + "/drawable-mdpi")) or
        (os.path.exists(directory + "/drawable-hdpi")) or
        (os.path.exists(directory + "/drawable-xhdpi")) or
        (os.path.exists(directory + "/drawable-xhdpi-v21")) or
        (os.path.exists(directory + "/drawable-xxhdpi")) or
        (os.path.exists(directory + "/drawable-xxxhdpi")))


def addString(string):
    string = string.replace("R.string.", "").replace("@string/", "")
    used_strings.add(string)


def addColor(color):
    color = color.replace("R.color.", "").replace("@color/", "")
    used_colors.add(color)


def addDrawableFile(fileName):
    fileName = fileName.replace("R.drawable.", "").replace("@drawable/", "").replace("R.mipmap.", "").replace("@mipmap/", "")
    used_drawable_files.add(fileName)


def addLayoutFile(fileName):
    fileName = fileName.replace("R.layout.", "").replace("@layout/", "").replace("R.menu.", "").replace("@menu/", "")
    used_layout_files.add(fileName)


def addResFile(fileName, resType):
    fileName = fileName.replace(
        "R." + resType + ".", "").replace("@" + resType + "/", "")
    used_res_files.add(fileName)


def checkFileForResources(fileAsString):
    '''
    Check to see what resources are referenced in this function.
    '''
    if not(
            fileAsString.endswith('.java') or
            fileAsString.endswith('.kt') or
            fileAsString.endswith('.xml')):
        return
    file = open(fileAsString, 'r')
    contents = file.read()
    file.close()

    # Handle code files.
    pattern = re.compile('R.string.[a-zA-Z0-9_]*')
    results = pattern.findall(contents)
    for result in results:
        addString(result)
    pattern = re.compile('R.color.[a-zA-Z0-9_]*')
    results = pattern.findall(contents)
    for result in results:
        addColor(result)
    pattern = re.compile('R.drawable.[a-zA-Z0-9_]*')
    results = pattern.findall(contents)
    for result in results:
        addDrawableFile(result)
    pattern = re.compile('R.mipmap.[a-zA-Z0-9_]*')
    results = pattern.findall(contents)
    for result in results:
        addDrawableFile(result)
    pattern = re.compile('R.layout.[a-zA-Z0-9_]*')
    results = pattern.findall(contents)
    for result in results:
        addLayoutFile(result)
    pattern = re.compile('R.menu.[a-zA-Z0-9_]*')
    results = pattern.findall(contents)
    for result in results:
        addLayoutFile(result)
    for resType in res_types:
        pattern = re.compile('R.' + resType + '.[a-zA-Z0-9_]*')
        results = pattern.findall(contents)
        for result in results:
            addResFile(result, resType)

    # Handle layout files.
    pattern = re.compile('@string/[a-zA-Z0-9_]*')
    results = pattern.findall(contents)
    for result in results:
        addString(result)
    pattern = re.compile('@color/[a-zA-Z0-9_]*')
    results = pattern.findall(contents)
    for result in results:
        addColor(result)
    pattern = re.compile('@drawable/[a-zA-Z0-9_]*')
    results = pattern.findall(contents)
    for result in results:
        addDrawableFile(result)
    pattern = re.compile('@mipmap/[a-zA-Z0-9_]*')
    results = pattern.findall(contents)
    for result in results:
        addDrawableFile(result)
    pattern = re.compile('@layout/[a-zA-Z0-9_]*')
    results = pattern.findall(contents)
    for result in results:
        addLayoutFile(result)
    pattern = re.compile('@menu/[a-zA-Z0-9_]*')
    results = pattern.findall(contents)
    for result in results:
        addLayoutFile(result)
    for resType in res_types:
        pattern = re.compile('@' + resType + '/[a-zA-Z0-9_]*')
        results = pattern.findall(contents)
        for result in results:
            addResFile(result, resType)


def deleteIfUnusedString(directory, fileName):
    print directory
    print fileName
    inFile = os.path.join(directory, fileName)
    outFile = os.path.join(directory, fileName + '.new')
    out = open(outFile, 'w')
    pattern = re.compile('\s+<string name="(.*?)".*?>.*?')
    for line in open(inFile, 'r'):
        match = pattern.match(line)
        if (match):
            if match.group(1) in used_strings:
                out.write(line)
        else:
            out.write(line)
    out.close()
    shutil.move(outFile, inFile)


def deleteIfUnusedColor(directory, fileName):
    print directory
    print fileName
    inFile = os.path.join(directory, fileName)
    outFile = os.path.join(directory, fileName + '.new')
    out = open(outFile, 'w')
    pattern = re.compile('\s+<color name="(.*?)".*?>.*?')
    for line in open(inFile, 'r'):
        match = pattern.match(line)
        if (match):
            color = match.group(1)
            if color in used_colors:
                out.write(line)
        else:
            out.write(line)
    out.close()
    shutil.move(outFile, inFile)


def deleteIfUnusedDrawable(directory, fileName):
    if (
            '/anim' in directory or
            '/color' in directory or
            '/layout' in directory or
            '/menu' in directory or
            '/values' in directory or
            '/raw' in directory or
            '/xml' in directory):
        return
    global files_deleted
    global kilo_bytes_deleted
    if fileName.endswith(".png"):
        originName = fileName
        if fileName.endswith('.9.png'):
            fileName = fileName[:-len('.9.png')]
        else:
            fileName = fileName[:-len('.png')]
        if fileName not in used_drawable_files:
            # Do stats tracking.
            files_deleted += 1
            current_file_size = os.path.getsize(
                directory + "/" + originName) / 1024.0
            kilo_bytes_deleted += current_file_size

            # Actually delete the file.
            os.unlink(directory + "/" + originName)
            print ("Deleted (%.2f Kbs) Drawable: " + directory +
                   "/" + originName) % current_file_size
    elif fileName.endswith(".xml"):
        originName = fileName
        fileName = fileName[:-len('.xml')]
        if fileName not in used_drawable_files:
            # Do stats tracking.
            files_deleted += 1
            current_file_size = os.path.getsize(
                directory + "/" + originName) / 1024.0
            kilo_bytes_deleted += current_file_size

            # Actually delete the file.
            os.unlink(directory + "/" + originName)
            print ("Deleted (%.2f Kbs) Drawable: " + directory +
                   "/" + originName) % current_file_size


def deleteIfUnusedLayout(directory, fileName):
    if not (
            '/layout' in directory or
            '/menu' in directory):
        return
    global files_deleted
    global kilo_bytes_deleted
    if fileName.endswith(".xml"):
        originName = fileName
        fileName = fileName[:-len('.xml')]
        if fileName not in used_layout_files:
            # Do stats tracking.
            files_deleted += 1
            current_file_size = os.path.getsize(
                directory + "/" + originName) / 1024.0
            kilo_bytes_deleted += current_file_size

            # Actually delete the file.
            os.unlink(directory + "/" + originName)
            print ("Deleted (%.2f Kbs) Layout: " + directory +
                   "/" + originName) % current_file_size


def deleteIfUnusedRes(directory, resType, fileName):
    if not (
            '/' + resType in directory):
        return
    global files_deleted
    global kilo_bytes_deleted
    if fileName.endswith(".xml"):
        originName = fileName
        fileName = fileName[:-len('.xml')]
        if fileName not in used_res_files:
            # Do stats tracking.
            files_deleted += 1
            current_file_size = os.path.getsize(
                directory + "/" + originName) / 1024.0
            kilo_bytes_deleted += current_file_size

            # Actually delete the file.
            os.unlink(directory + "/" + originName)
            print ("Deleted (%.2f Kbs) Res: " + directory +
                   "/" + originName) % current_file_size

##########
#  MAIN  #
##########

# Make sure they passed in a project source directory.
if not len(sys.argv) == 2:
    print 'Usage: "python clean_res.py project_src_directory"'
    quit()


def clean():
    rootDirectory = sys.argv[1]
    resDirectory = []

    # Figure out which resources are actually used.
    for root, dirs, files in os.walk(rootDirectory):
        if isIgnoredRoot(root):
            continue
        elif isResourceRoot(root):
            print 'resource-root:', root
            resDirectory.append(root)

        for file in files:
            checkFileForResources(root + "/" + file)

    # Delete the unused pngs.
    for directory in resDirectory:
        for root, dirs, files in os.walk(directory):
            for file in files:
                if file == 'strings.xml':
                    deleteIfUnusedString(root, file)
                if file == 'colors.xml':
                    deleteIfUnusedColor(root, file)
                deleteIfUnusedDrawable(root, file)
                deleteIfUnusedLayout(root, file)
                for resType in res_types:
                    deleteIfUnusedRes(root, resType, file)

current_deleted_files_count = 0
clean()
# run until no more files is added to deleted set:
while current_deleted_files_count < files_deleted:
    current_deleted_files_count = files_deleted
    used_strings = set()
    used_colors = set()
    used_drawable_files = set()
    used_layout_files = set()
    used_res_files = set()
    clean()

# Print out how many files were actually deleted.
print ""
print "%d file(s) deleted" % (files_deleted)
print "%.2f kilobytes freed" % (kilo_bytes_deleted)
