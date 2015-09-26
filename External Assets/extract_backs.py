import os
import shutil
import subprocess
from sys import stdout
from distutils.dir_util import copy_tree

cwd = os.getcwd()

ids = [ "g41939",
        "g17924",
        "g7610",
        "g41947",
        "g41955",
        "g7647",
        "g9756",
        "g32361",
        "g9761",
        "g37108",
        "g4965",
        "g4965-8",
        "g4965-6",
        "g53943",
        "g23876",
        "g25281",
        "g13668-1",
        "g13668-7",
        "g13668-77",
        "g25889",
        "g18951",
        "layer1-7",
        "g9326",
        "g9358",
        "g36605",
        "g9342",
        "g9318",
        "g13590",
        "g13490",
        "g13390",
        "g9798",
        "g13290" ]

inputFile = cwd + "/backs.svg"
outputFolder = cwd + "/backs"

i = 1
for id in ids:
    outputFile = outputFolder + "/back" + str( i ) + ".svg"
    stdout.write( "Exporting " + id + "..." )
    params = [
                'C:\Program Files\Inkscape\inkscape.exe',
                '--file', inputFile,
                '--export-plain-svg', outputFile,
                '--export-id', id,
                '--export-id-only'
             ]
    if subprocess.call( params ) == 0:
        stdout.write( "Success\n" )
    else:
        stdout.write( "Failure\n" )
        break

    stdout.write( "    Cropping " + id + "..." )
    params = [
                'C:\Program Files\Inkscape\inkscape.exe',
                '--file', outputFile,
                '--verb=FitCanvasToDrawing',
                '--verb=FileSave',
                '--verb=FileQuit'
             ]
    if subprocess.call( params ) == 0:
        stdout.write( "Success\n" )
    else:
        stdout.write( "Failure\n" )
        break

    i += 1

raw_input( "\nPress Enter to continue..." )