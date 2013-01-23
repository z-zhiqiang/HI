package sun.processor.predicate.processor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import sun.processor.predicate.PredicateDataSet;
import sun.processor.predicate.PredicateDataSet.Run;
import sun.processor.predicate.PredicateItem.PredicateKey;

public class PredicateDataSetMappingOutputter extends
		AbstractPredicateDataSetProcessor {

	private final File profileMappingFile;
	private final File predicateMappingFile;

	public PredicateDataSetMappingOutputter(File profileMappingFile,
			File predicateMappingFile) {
		super();
		this.profileMappingFile = profileMappingFile;
		this.predicateMappingFile = predicateMappingFile;
	}

	@Override
	protected void processPredicateDataSet(PredicateDataSet dataset) {
		BufferedWriter profileMappingWriter = null;
		BufferedWriter predicateMappingWriter = null;
		final String space = "    ";
		try {
			profileMappingWriter = new BufferedWriter(new FileWriter(
					this.profileMappingFile));
			for (Run run : dataset.getRuns()) {
				profileMappingWriter.write("Profile ");
				profileMappingWriter.write(String.valueOf(run.getId()));
				profileMappingWriter.write(":\n");
				profileMappingWriter.write(space);
				profileMappingWriter.write(run.getProfile().toString());
				profileMappingWriter.newLine();
				profileMappingWriter.newLine();
			}
			profileMappingWriter.close();
			profileMappingWriter = null;

			predicateMappingWriter = new BufferedWriter(new FileWriter(
					this.predicateMappingFile));
			for (PredicateKey k : dataset.getKeys()) {
				predicateMappingWriter.write("Predicate ");
				predicateMappingWriter.write(String.valueOf(k.getId()));
				predicateMappingWriter.write(":\n");
				predicateMappingWriter.write(space);
				predicateMappingWriter.write(k.toString());
				predicateMappingWriter.newLine();
				predicateMappingWriter.write(space);
				predicateMappingWriter.write(k.getSite().toStringWithoutFile());
				predicateMappingWriter.newLine();
				predicateMappingWriter.write(space);
				predicateMappingWriter.write(k.getSite().getFileString());
				// predicateMappingWriter.write(k.getSite().toString());
				predicateMappingWriter.newLine();
				predicateMappingWriter.newLine();
			}
			predicateMappingWriter.close();
			predicateMappingWriter = null;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (profileMappingWriter != null)
				try {
					profileMappingWriter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			if (predicateMappingWriter != null)
				try {
					predicateMappingWriter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}

}
