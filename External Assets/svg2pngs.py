from subprocess import call
import os
import shutil

i_to_a = { "2"  : "two", 
           "3"  : "three",
           "4"  : "four",
           "5"  : "five",
           "6"  : "six", 
           "7"  : "seven",
           "8"  : "eight",
           "9"  : "nine",
           "10" : "ten" };

cwd = os.getcwd()

inputFolder = cwd + "/svg_cards/"
outputFolder = cwd + "/export/"
outputFolders = [ "/drawable-mdpi/", "/drawable-hdpi/", "/drawable-xhdpi/", "/drawable-xxhdpi/" ]
cardSize = 100
pixelSizes = [ cardSize * 160 / 160, cardSize * 240 / 160, cardSize * 320 / 160, cardSize * 480 / 160 ]
for d in outputFolders:
    if not os.path.isdir( outputFolder + d ):
        os.makedirs( outputFolder + d )

for filename in os.listdir( inputFolder ):
    if filename.endswith( ".svg" ):
        newFilename = filename.replace( ".svg", ".png" )
        for key in i_to_a:
            newFilename = newFilename.replace( key, i_to_a[ key ] )
        print filename, "\t->\t", newFilename
        filename = inputFolder + filename

        for i in range( len( outputFolders ) ):
            outFile = outputFolder + outputFolders[ i ] + newFilename

            call( 'C:\Program Files\Inkscape\inkscape.exe -f ' + filename + ' -e ' + outFile + ' -D -w ' + str( pixelSizes[ i ] ) )
    break # For testing