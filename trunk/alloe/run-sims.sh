#!/bin/bash

java -cp dist/ALLOE.jar nii.alloe.runs.Simulations logics/synonym.logic synbig 50 2001 no yes
java -cp dist/ALLOE.jar nii.alloe.runs.Simulations logics/hypernym.logic hypsmall 5 101 yes no
java -cp dist/ALLOE.jar nii.alloe.runs.Simulations logics/hypernym.logic hypbig 50 2001 no no
java -cp dist/ALLOE.jar nii.alloe.runs.Simulations logics/sh.logic shsmall 5 101 yes no
java -cp dist/ALLOE.jar nii.alloe.runs.Simulations logics/sh.logic shbig 50 2001 no no
