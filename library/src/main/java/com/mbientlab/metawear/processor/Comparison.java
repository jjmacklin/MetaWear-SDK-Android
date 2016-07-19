/*
 * Copyright 2014-2015 MbientLab Inc. All rights reserved.
 *
 * IMPORTANT: Your use of this Software is limited to those specific rights granted under the terms of a software
 * license agreement between the user who downloaded the software, his/her employer (which must be your
 * employer) and MbientLab Inc, (the "License").  You may not use this Software unless you agree to abide by the
 * terms of the License which can be found at www.mbientlab.com/terms.  The License limits your use, and you
 * acknowledge, that the Software may be modified, copied, and distributed when used in conjunction with an
 * MbientLab Inc, product.  Other than for the foregoing purpose, you may not use, reproduce, copy, prepare
 * derivative works of, modify, distribute, perform, display or sell this Software and/or its documentation for any
 * purpose.
 *
 * YOU FURTHER ACKNOWLEDGE AND AGREE THAT THE SOFTWARE AND DOCUMENTATION ARE PROVIDED "AS IS" WITHOUT WARRANTY
 * OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION, ANY WARRANTY OF MERCHANTABILITY, TITLE,
 * NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE. IN NO EVENT SHALL MBIENTLAB OR ITS LICENSORS BE LIABLE OR
 * OBLIGATED UNDER CONTRACT, NEGLIGENCE, STRICT LIABILITY, CONTRIBUTION, BREACH OF WARRANTY, OR OTHER LEGAL EQUITABLE
 * THEORY ANY DIRECT OR INDIRECT DAMAGES OR EXPENSES INCLUDING BUT NOT LIMITED TO ANY INCIDENTAL, SPECIAL, INDIRECT,
 * PUNITIVE OR CONSEQUENTIAL DAMAGES, LOST PROFITS OR LOST DATA, COST OF PROCUREMENT OF SUBSTITUTE GOODS, TECHNOLOGY,
 * SERVICES, OR ANY CLAIMS BY THIRD PARTIES (INCLUDING BUT NOT LIMITED TO ANY DEFENSE THEREOF), OR OTHER SIMILAR COSTS.
 *
 * Should you have any questions regarding your right to use this Software, contact MbientLab via email:
 * hello@mbientlab.com.
 */

package com.mbientlab.metawear.processor;

import com.mbientlab.metawear.DataSignal;

import java.util.Map;

/**
 * Configuration for the comparison data processor
 * @author Eric Tsai
 */
public class Comparison implements DataSignal.ProcessorConfig {
    public static final String SCHEME_NAME= "comparison";
    public final static String FIELD_OP= "operation", FIELD_SIGNED= "signed", FIELD_REFERENCE="reference", FIELD_MODE="mode";

    /**
     * Supported comparison operations for the processor
     * @author Eric Tsai
     */
    public enum Operation {
        /** Equal */
        EQ,
        /** Not equal */
        NEQ,
        /** Less than */
        LT,
        /** Less than or equal to */
        LTE,
        /** Greater than */
        GT,
        /** Greater than or equal to */
        GTE,
    }

    /**
     * Operation modes for multi-valued comparison, only used on firmware v1.2.3 or later
     * @author Eric Tsai
     */
    public enum Mode {
        /** Input value is returned when the comparison is satisfied */
        ABSOLUTE,
        /** The reference value that satisfies the comparison is returned, no output if none match */
        REFERENCE,
        /** The index (0 based) of the value that satisfies the comparison is returned, n if none match */
        ZONE,
        /** 0 if comparison failed, 1 if it passed */
        PASS_FAIL
    }

    public final DataSignal.DataToken referenceToken;
    public final Boolean signed;
    public final Operation compareOp;
    public final Number[] reference;
    public final Mode mode;

    /**
     * Constructs a comparison config object from a URI string
     * @param query    String-String map containing the fields from the URI string
     */
    public Comparison(Map<String, String> query) {
        referenceToken= null;

        if (!query.containsKey(FIELD_OP)) {
            throw new RuntimeException("Missing required field in URI: " + FIELD_OP);
        } else {
            compareOp= Enum.valueOf(Operation.class, query.get(FIELD_OP).toUpperCase());
        }

        if (query.containsKey(FIELD_SIGNED)) {
            signed= Boolean.valueOf(query.get(FIELD_SIGNED).toUpperCase());
        } else {
            signed= null;
        }

        if (query.containsKey(FIELD_MODE)) {
            mode= Enum.valueOf(Mode.class, query.get(FIELD_MODE).toUpperCase());
        } else {
            mode= Mode.ABSOLUTE;
        }

        if (!query.containsKey(FIELD_REFERENCE)) {
            throw new RuntimeException("Missing required field in URI: " + FIELD_REFERENCE);
        } else {
            String[] values= query.get(FIELD_REFERENCE).split(",");
            reference= new Number[values.length];
            for(int i= 0; i < values.length; i++) {
                if (values[i].contains(".")) {
                    reference[i]= Float.valueOf(query.get(FIELD_REFERENCE));
                } else {
                    reference[i]= Integer.valueOf(query.get(FIELD_REFERENCE));
                }
            }

        }
    }

    /**
     * Constructs a config object with inferred signed or unsigned comparison
     * @param op Comparison operation to filter on
     * @param reference Value to compare against
     */
    public Comparison(Operation op, Number ... reference) {
        this(op, Mode.ABSOLUTE, null, reference);
    }

    /**
     * Constructs a config object with inferred signed or unsigned comparison.  This constructor is
     * for updating a comparison filter in a feedback or feedforward loop
     * @param op Comparison operation to filter on
     * @param reference Token representing the sensor data to be used for the reference value
     */
    public Comparison(Operation op, DataSignal.DataToken reference) {
        this(op, reference, null);
    }

    /**
     * Constructs a config object with user explicitly requesting a signed or unsigned comparison
     * @param op Comparison operation to filter on
     * @param reference Value to compare against
     * @param signed True if a signed comparison should be used, false for unsigned
     */
    public Comparison(Operation op, Number reference, Boolean signed) {
        this(op, Mode.ABSOLUTE, signed, reference);
    }

    /**
     * Constructs a config object for multi-valued comparisons with inferred signed/unsigned comparison,
     * only supported for firmware v1.2.3 or later
     * @param op            Comparison operation
     * @param mode          Operation mode
     * @param references    Values to compare against
     */
    public Comparison(Operation op, Mode mode, Number ... references) {
        this(op, mode, null, references);
    }
    /**
     * Constructs a config object for multi-valued comparisons with user explicitly requesting signed/unsigned comparison,
     * only supported for firmware v1.2.3 or later
     * @param op            Comparison operation
     * @param mode          Operation mode
     * @param references    Values to compare against
     */
    public Comparison(Operation op, Mode mode, Boolean signed, Number ... references) {
        this.compareOp= op;
        this.mode= mode;
        this.reference= references;
        this.referenceToken= null;
        this.signed= signed;
    }

    /**
     * Constructs a config object with user explicitly requesting a signed or unsigned comparison.  This constructor is
     * for updating a comparison filter in a feedback or feedforward loop
     * @param op Comparison operation to filter on
     * @param reference Token representing the sensor data to be used for the reference value
     * @param signed True if a signed comparison should be used, false for unsigned
     */
    public Comparison(Operation op, DataSignal.DataToken reference, Boolean signed) {
        this.compareOp= op;
        this.reference= new Number[] {0};
        this.referenceToken= reference;
        this.signed= signed;
        this.mode= Mode.ABSOLUTE;
    }
}
