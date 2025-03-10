package softClipBam;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import htsjdk.samtools.Cigar;
import htsjdk.samtools.CigarElement;
import htsjdk.samtools.CigarOperator;
import htsjdk.samtools.SAMException;
import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.util.CigarUtil;

/* 

5' +++++>>>>R1>>>>+++++++++++++++++++++++++++++>>>>R2>>>>++++++++++++++++++++ 3'
3' ---------------<<<<R2<<<<--------------------------------<<<<R1<<<<------- 5'

MEMO: Read 1+ is handled in the same way as Read 2+ and Read 1- same as Read 2-. 
*/


public class Clipper {
	
	public static SAMRecord clip(SAMRecord rec, int R1_5p, int R1_3p, int R2_5p, int R2_3p) {
    			
		int r1_3p= (rec.getReadLength() - R1_3p+1) < 0 ? 1 : rec.getReadLength() - R1_3p+1;
    	int r2_3p= (rec.getReadLength() - R2_3p+1) < 0 ? 1 : rec.getReadLength() - R2_3p+1;

    	if(!cigarHasMappedBases(rec.getCigar())){
    		// If a read has already no aligned bases, e.g. after bam clipOverlap, reset it
    		// to be consistent with softClip3PrimeEndOfRead();
    		resetReadToNoAlignedBases(rec);
    	} else if (!rec.getReadPairedFlag() || rec.getFirstOfPairFlag()){
    		// Read single end reads or Read first in pair
			if(R1_5p > 0 && cigarHasMappedBases(rec.getCigar())) rec= softClip5PrimeEndOfRead(rec, R1_5p);
    		if(R1_3p > 0 && cigarHasMappedBases(rec.getCigar())) CigarUtil.softClip3PrimeEndOfRead(rec, r1_3p);    	
    	} else if(rec.getReadPairedFlag() && rec.getSecondOfPairFlag()){
    		// Read 2
    		if(R2_5p > 0 && cigarHasMappedBases(rec.getCigar())) rec= softClip5PrimeEndOfRead(rec, R2_5p);
    		if(R2_3p > 0 && cigarHasMappedBases(rec.getCigar())) CigarUtil.softClip3PrimeEndOfRead(rec, r2_3p);
    	} else {
    		System.err.println("Unexpected case");
    		System.exit(1);
    	}
    	return rec;		
	}
	
	/* P R I V A T E   M E T H O D S */
	
	private static SAMRecord softClip5PrimeEndOfRead(SAMRecord rec, int clipUpTo){
		
		if(clipUpTo > 0){
			final Cigar cigar = rec.getCigar();
			boolean isPositiveStrand= !rec.getReadNegativeStrandFlag();
			int clipFrom= (rec.getReadLength() + 1) - clipUpTo;
			//if(clipFrom < 1){
			//	clipFrom= 1;
			//}
			if (isPositiveStrand){
				// Trim from 5' using softClip3PrimeEndOfRead method.
				// For this you need to invert the cigar, clip, invert again.
				rec.setCigar(invertCigar(rec.getCigar()));
				CigarUtil.softClip3PrimeEndOfRead(rec, clipFrom);
				Cigar invCigar= invertCigar(rec.getCigar());
				rec.setCigar(invCigar);

	        	// Adjust alignment start.
				// See also code for CigarUtil.softClip3PrimeEndOfRead();
	            int oldLength = cigar.getReferenceLength();
	            int newLength = rec.getCigar().getReferenceLength();
	            int sizeChange = oldLength - newLength;
	            if (sizeChange > 0){
	                rec.setAlignmentStart(rec.getAlignmentStart() + sizeChange);
	            } else if (sizeChange < 0){
	                throw new SAMException("The clipped length " + newLength +
	                        " is longer than the old unclipped length " + oldLength);
	            }
	        } else {
	        	// Read reverse strand: No need to adjust aln start
	        	// Just clip from end.
	        	Cigar clipCigar= new Cigar(CigarUtil.softClipEndOfRead(clipFrom, rec.getCigar().getCigarElements()));
	        	rec.setCigar(clipCigar);
	        	if(!cigarHasMappedBases(clipCigar)){
	        		resetReadToNoAlignedBases(rec);
	        	} else {
	        		rec.setCigar(clipCigar);
	        	}
	        }
		}
		return rec;
	}
	
	private static Cigar invertCigar(Cigar cigar){
		List<CigarElement> cigarList= cigar.getCigarElements();
		// Make a copy of cigarList since it is unmodifiable. 
		cigarList= new ArrayList<CigarElement>(cigarList);
		Collections.reverse(cigarList);
		Cigar invCigar= new Cigar(cigarList);
		return invCigar;
	}	
	
	/**
	 * Return true if cigar has mapped bases. Returns false when for example
	 * cigar is entirely soft clipped like "50S", which can happen after 
	 * clipping with bam clipOverlap.
	 * This code is largely from CigarUtil.softClip3PrimeEndOfRead();
	 * @param cigar
	 * @return
	 */
	private static boolean cigarHasMappedBases(Cigar cigar){
        boolean hasMappedBases = false;
        for (final CigarElement elem : cigar.getCigarElements()) {
            final CigarOperator op = elem.getOperator();
            if (op.consumesReferenceBases() && op.consumesReadBases()) {
                hasMappedBases = true;
                break;
            }
        }
        return hasMappedBases;
	}
	
	/**
	 * If a read has no aligned bases after trimming, reset attributes 
	 * to be consistent with output of CigarUtil.softClip3PrimeEndOfRead();
	 * @param rec
	 * @return
	 */
	private static SAMRecord resetReadToNoAlignedBases(SAMRecord rec){
		rec.setReadUnmappedFlag(true);
		rec.setCigarString(SAMRecord.NO_ALIGNMENT_CIGAR);
		rec.setReferenceIndex(SAMRecord.NO_ALIGNMENT_REFERENCE_INDEX);
		rec.setAlignmentStart(SAMRecord.NO_ALIGNMENT_START);
		rec.setMappingQuality(SAMRecord.NO_MAPPING_QUALITY);
		rec.setInferredInsertSize(0);
		return rec;
	}
}
