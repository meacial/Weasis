/*******************************************************************************
 * Copyright (c) 2017 Weasis Team.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Nicolas Roduit - initial API and implementation
 *     Tomas Skripcak - initial API and implementation
 ******************************************************************************/

package org.weasis.dicom.rt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.weasis.core.api.media.data.MediaElement;
import org.weasis.dicom.codec.DicomImageElement;

public class Dose extends HashMap<Integer, Dvh> {
    private static final long serialVersionUID = 1L;

    private String sopInstanceUid;
    private double[] imagePositionPatient;
    private String comment;
    private String doseUnit;
    private String doseType;
    private String doseSummationType;
    private double[] gridFrameOffsetVector;
    private double doseGridScaling;
    private double doseMax;
    private String referencedPlanUid;

    private double doseSlicePositionThreshold;

    private List<MediaElement> images = new ArrayList<>();
    private HashMap<Integer, IsoDoseLayer> isoDoseSet = new HashMap<>();

    public Dose() {
        // Default threshold in mm to determine the max difference from slicePosition to closest dose frame without interpolation
        this.doseSlicePositionThreshold = 0.5;
    }

    public String getSopInstanceUid() {
        return sopInstanceUid;
    }

    public void setSopInstanceUid(String sopInstanceUid) {
        this.sopInstanceUid = sopInstanceUid;
    }

    public double[] getImagePositionPatient() {
        return imagePositionPatient;
    }

    public void setImagePositionPatient(double[] imagePositionPatient) {
        this.imagePositionPatient = imagePositionPatient;
    }

    public String getComment() {
        return this.comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getDoseUnit() {
        return this.doseUnit;
    }

    public void setDoseUnit(String doseUnit) {
        this.doseUnit = doseUnit;
    }

    public String getDoseType() {
        return doseType;
    }

    public void setDoseType(String doseType) {
        this.doseType = doseType;
    }

    public String getDoseSummationType() {
        return doseSummationType;
    }

    public void setDoseSummationType(String doseSummationType) {
        this.doseSummationType = doseSummationType;
    }

    public double[] getGridFrameOffsetVector() {
        return this.gridFrameOffsetVector;
    }

    public void setGridFrameOffsetVector(double[] gridFrameOffsetVector) {
        this.gridFrameOffsetVector = gridFrameOffsetVector;
    }

    public double getDoseGridScaling() {
        return doseGridScaling;
    }

    public void setDoseGridScaling(double doseGridScaling) {
        this.doseGridScaling = doseGridScaling;
    }

    public double getDoseMax() {
        return doseMax;
    }

    public void setDoseMax(double doseMax) {
        this.doseMax = doseMax;
    }

    public String getReferencedPlanUid() {
        return this.referencedPlanUid;
    }

    public void setReferencedPlanUid(String referencedPlanUid) {
        this.referencedPlanUid = referencedPlanUid;
    }

    public double getDoseSlicePositionThreshold() {
        return this.doseSlicePositionThreshold;
    }

    public void setDoseSlicePositionThreshold(double doseSlicePositionThreshold) {
        this.doseSlicePositionThreshold = doseSlicePositionThreshold;
    }

    public List<MediaElement> getImages() {
        return this.images;
    }

    public void setImages(List<MediaElement> images) {
        this.images = images;
    }

    public HashMap<Integer, IsoDoseLayer> getIsoDoseSet() {
        return isoDoseSet;
    }

    public void setIsoDoseSet(HashMap<Integer, IsoDoseLayer> isoDoseSet) {
        this.isoDoseSet = isoDoseSet;
    }

    public MediaElement getDosePlaneBySlice(double slicePosition) {
        MediaElement dosePlane = null;

        // If dose contains a multi-frame dose pixel array
        if (this.gridFrameOffsetVector.length > 0) {

            // Initial dose grid position Z (in patient coordinates)
            double imagePatientPositionZ = this.imagePositionPatient[2];

            // Add initial image patient position Z to the offset vector to determine the Z coordinate of each dose plane
            double[] dosePlanesZ = new double[this.gridFrameOffsetVector.length];
            for (int i = 0; i < dosePlanesZ.length; i++) {
                dosePlanesZ[i] = this.gridFrameOffsetVector[i]  + imagePatientPositionZ;
            }

            // Check whether the requested plane is within the dose grid boundaries
            if (Arrays.stream(dosePlanesZ).min().getAsDouble() <= slicePosition && slicePosition <= Arrays.stream(dosePlanesZ).max().getAsDouble()) {

                // Calculate the absolute distance vector between dose planes and requested slice position
                double[] absoluteDistance = new double[dosePlanesZ.length];
                for (int i = 0; i < absoluteDistance.length; i++) {
                    absoluteDistance[i] = Math.abs(dosePlanesZ[i] - slicePosition);
                }

                // Check to see if the requested plane exists in the array (or is close enough)
                int doseSlicePosition = -1;
                double minDistance = Arrays.stream(absoluteDistance).min().getAsDouble();
                if (minDistance < this.doseSlicePositionThreshold) {
                    doseSlicePosition = firstIndexOf(absoluteDistance, minDistance, 0.001);
                }

                // Dose slice position found return the plane
                if (doseSlicePosition != -1) {
                    dosePlane = this.images.get(doseSlicePosition);
                }
                // There is no dose plane for such slice position, so interpolate between planes
                else {

                    // First minimum distance - upper boundary
                    int upperBoundaryIndex = firstIndexOf(absoluteDistance, minDistance, 0.001);

                    // Prepare modified absolute distance vector to find the second minimum
                    double[] modifiedAbsoluteDistance = Arrays.copyOf(absoluteDistance, absoluteDistance.length);
                    modifiedAbsoluteDistance[upperBoundaryIndex] = Arrays.stream(absoluteDistance).max().getAsDouble();

                    // Second minimum distance - lower boundary
                    minDistance = Arrays.stream(modifiedAbsoluteDistance).min().getAsDouble();
                    int lowerBoundaryIndex = firstIndexOf(modifiedAbsoluteDistance, minDistance, 0.001);

                    // Fractional distance of dose plane between upper and lower boundary (from bottom to top)
                    // E.g. if = 1, the plane is at the upper plane, = 0, it is at the lower plane.
                    double fractionalDistance = (slicePosition - dosePlanesZ[lowerBoundaryIndex]) / (dosePlanesZ[upperBoundaryIndex] - dosePlanesZ[lowerBoundaryIndex]);

                    dosePlane = this.interpolateDosePlanes(upperBoundaryIndex, lowerBoundaryIndex, fractionalDistance);
                }
            }
        }

        return dosePlane;
    }

    public int[] getIsoDosePoints(double slicePosition, double isoDoseThreshold) {
        DicomImageElement dosePlane = (DicomImageElement) this.getDosePlaneBySlice(slicePosition);

        int minX = dosePlane.getImage().getMinX();
        int maxX = dosePlane.getImage().getMaxX();
        int minY = dosePlane.getImage().getMinY();
        int maxY = dosePlane.getImage().getMaxY();

        List<Integer> listX = new ArrayList<>();
        List<Integer> listY = new ArrayList<>();

        // Go through dose plane
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {

                // Dose voxel value
                int[] rawDoseVoxelValue = dosePlane.getImage().getData().getPixel(x, y, (int[]) null);
                if (rawDoseVoxelValue.length == 1) {

                    double doseVoxelValue = rawDoseVoxelValue[0] * this.getDoseGridScaling();

                    // Check which voxel values from dosePlane satisfy to iso dose threshold
                    if (doseVoxelValue >= isoDoseThreshold) {
                        listX.add(x);
                        listY.add(y);
                    }
                }
            }
        }

        // collect indices of iso points in array
        int size = listX.size();
        int[] isoPoints = new int[size * 2];
        int j = 0;
        for (int i = 0; i < size; i++) {
            isoPoints[j] = listX.get(i);
            isoPoints[j + 1] = listY.get(i);
            j+=2;
        }

        return isoPoints;
    }

    public int[] getIsoDoseContourPoints(double slicePosition, double isoDoseThreshold) {
        DicomImageElement dosePlane = (DicomImageElement) this.getDosePlaneBySlice(slicePosition);

        int minX = dosePlane.getImage().getMinX();
        int maxX = dosePlane.getImage().getMaxX();
        int minY = dosePlane.getImage().getMinY();
        int maxY = dosePlane.getImage().getMaxY();

        byte[] binaryArray = new byte[maxX * maxY];

        // Calculate the distance between each iso dose point and threshold
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {

                // Dose voxel value
                int[] rawDoseVoxelValue = dosePlane.getImage().getData().getPixel(x, y, (int[]) null);
                if (rawDoseVoxelValue.length == 1) {

                    double doseVoxelValue = rawDoseVoxelValue[0] * this.getDoseGridScaling();

                    if (doseVoxelValue >= isoDoseThreshold) {
                        binaryArray[x*y] = 1;
                    }
                    else {
                        binaryArray[x*y] = 0;
                    }
                }
            }
        }

        // TODO: the minimal distance will be border points for contour
        
        return new int[0];
    }

    private MediaElement interpolateDosePlanes(int upperBoundaryIndex, int lowerBoundaryIndex, double fractionalDistance) {
        MediaElement dosePlane = null;

        MediaElement upperPlane = this.images.get(upperBoundaryIndex);
        MediaElement lowerPlane = this.images.get(lowerBoundaryIndex);
        
        // A simple linear interpolation
        //TODO: dosePlane = fractionalDistance * upperPlane + (1.0 - fractionalDistance) * lowerPlane;

        return dosePlane;
    }

    private static int firstIndexOf(double[] array, double valueToFind, double tolerance) {
        for(int i = 0; i < array.length; i++) {
            if (Math.abs(array[i] - valueToFind) < tolerance) {
                return i;
            }
        }
        return -1;
    }
}