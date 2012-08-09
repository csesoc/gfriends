#!/usr/local/bin/bash

cd /tmp
pdflatex cheatsheet.tex
chmod 777 cheatsheet.*
xpdf cheatsheet.pdf
