import os
import shutil
from subprocess import call
from sys import stdout
from distutils.dir_util import copy_tree

DEFAULT_DPI = 160

i_to_a = { "2"  : "two", 
           "3"  : "three",
           "4"  : "four",
           "5"  : "five",
           "6"  : "six", 
           "7"  : "seven",
           "8"  : "eight",
           "9"  : "nine",
           "10" : "ten" };

cardWidth = 90
cardHeight = cardWidth * 3.5 / 2.5
outputFolders = [ "/drawable-mdpi/",    "/drawable-hdpi/",  "/drawable-xhdpi/",     "/drawable-xxhdpi/"     ]
outputDpis =    [ 160,                  240,                320,                    480                     ]

cwd = os.getcwd()

inputFolder = cwd + "/svg_cards/"
outputFolder = cwd + "/export/"
resourceFolder = cwd + "/../app/src/main/res/"

for d in outputFolders:
    if not os.path.isdir( outputFolder + d ):
        os.makedirs( outputFolder + d )

for filename in os.listdir( inputFolder ):
    if filename.endswith( ".svg" ):
        newFilename = filename.replace( ".svg", ".png" )
        for key in i_to_a:
            newFilename = newFilename.replace( key, i_to_a[ key ] )
        print filename, "    ->    ", newFilename

        filename = inputFolder + filename
        for i in range( len( outputFolders ) ):
            outFile = outputFolder + outputFolders[ i ] + newFilename
            width = cardWidth * outputDpis[ i ] / DEFAULT_DPI
            height = cardHeight * outputDpis[ i ] / DEFAULT_DPI
            stdout.write( "    " + outputFolders[ i ] + "..." )
            ret = call( [ 'C:\Program Files\Inkscape\inkscape.exe', '-f', filename, '-e', outFile, '-D', '-w', str( width ), '-h', str( height ) ] )
            if ret == 0:
                stdout.write( "Success\n" )
            else:
                write( "Failure!\n" )
                
done = False
while not done:
    response = raw_input( "\nCopy to Android resource folder? (Y/N): " ).upper()
    if len( response ) > 0:
        if response[ 0 ] == "Y":
            stdout.write( "    Copying..." )
            ret = copy_tree( outputFolder, resourceFolder )
            if ret:
                stdout.write( "Success\n" )
            else:
                stdout.write( "Failure!\n" )
            done = True
        elif response[ 0 ] == "N":
            done = True
    if not done:
        stdout.write( "    Invalid response." )