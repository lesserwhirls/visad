package visad.data.visad.object;

import java.io.DataInput;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;

import visad.Data;
import visad.DataImpl;
import visad.FieldImpl;
import visad.FunctionType;
import visad.Set;
import visad.VisADException;

import visad.data.visad.BinaryObjectCache;
import visad.data.visad.BinaryReader;
import visad.data.visad.BinaryWriter;

public class BinaryFieldImpl
  implements BinaryObject
{
  public static final int computeBytes(FieldImpl fld)
  {
    try {
      return processDependentData(null, null, fld.getDomainSet(), fld);
    } catch (IOException ioe) {
      return 0;
    }
  }

  public static final int processDependentData(BinaryWriter writer,
                                               FunctionType ft, Set set,
                                               FieldImpl fld)
    throws IOException
  {
    byte dataType;
    if (!fld.getClass().equals(FieldImpl.class)) {
      return 0;
    }

    int numBytes = 1 + 4;

if(DEBUG_WR_DATA&&!DEBUG_WR_MATH)System.err.println("wrFldI: type (" + ft + ")");
    if (writer != null) {
      BinaryFunctionType.write(writer, ft, SAVE_DATA);
    }
    numBytes += 1 + 4;

    if (set != null) {
      if (writer != null) {
        BinaryGeneric.write(writer, set, SAVE_DEPEND);
      }

      int setBytes = BinaryGeneric.computeBytes(set);
      if (setBytes > 0) {
        numBytes += 1 + setBytes;
      }
    }

    final int numSamples = (fld.isMissing() ? 0 : fld.getLength());
    if (numSamples > 0) {
      numBytes += 1;

      for (int i = 0; i < numSamples; i++) {
        DataImpl sample;
        try {
          sample = (DataImpl )fld.getSample(i);
        } catch (VisADException ve) {
          continue;
        }

        if (writer != null) {
          BinaryGeneric.write(writer, sample, SAVE_DEPEND);
        }

        numBytes += BinaryGeneric.computeBytes(sample);
      }
    }

    return numBytes;
  }

  public static final FieldImpl read(BinaryReader reader)
    throws IOException, VisADException
  {
    BinaryObjectCache cache = reader.getTypeCache();
    DataInput file = reader.getInput();

    final int typeIndex = file.readInt();
if(DEBUG_RD_DATA&&DEBUG_RD_MATH)System.err.println("rdFldI: type index (" + typeIndex + ")");
    FunctionType ft = (FunctionType )cache.get(typeIndex);
if(DEBUG_RD_DATA&&!DEBUG_RD_MATH)System.err.println("rdFldI: type index (" + typeIndex + "=" + ft + ")");

    Set set = null;
    Data[] samples = null;

    boolean reading = true;
    while (reading) {
      final byte directive;
      try {
        directive = file.readByte();
      } catch (EOFException eofe) {
        return null;
      }

      switch (directive) {
      case FLD_SET:
if(DEBUG_RD_DATA)System.err.println("rdFldI: FLD_SET (" + FLD_SET + ")");
        set = (Set )BinaryGeneric.read(reader);
        break;
      case FLD_DATA_SAMPLES:
if(DEBUG_RD_DATA)System.err.println("rdFldI: FLD_DATA_SAMPLES (" + FLD_DATA_SAMPLES + ")");
        final int numSamples = file.readInt();
if(DEBUG_RD_DATA)System.err.println("rdFldI: numSamples (" + numSamples + ")");
        if (numSamples <= 0) {
          throw new IOException("Corrupted file (bad Field sample length " +
                                numSamples + ")");
        }

        samples = new Data[numSamples];
        for (int i = 0; i < numSamples; i++) {
if(DEBUG_WR_DATA)System.err.println("rdFldI#"+i);
          samples[i] = BinaryGeneric.read(reader);
if(DEBUG_WR_DATA_DETAIL)System.err.println("rdFldI: #" + i + " (" + samples[i] + ")");
        }
        break;
      case FLD_END:
if(DEBUG_RD_DATA)System.err.println("rdFldI: FLD_END (" + FLD_END + ")");
        reading = false;
        break;
      default:
        throw new IOException("Unknown FieldImpl directive " +
                              directive);
      }
    }

    if (ft == null) {
      throw new IOException("No FunctionType found for FieldImpl");
    }

    FieldImpl fld = (set == null ? new FieldImpl(ft) :
                     new FieldImpl(ft, set));
    if (samples != null) {
      final int len = samples.length;
      for (int i = 0; i < len; i++) {
        fld.setSample(i, samples[i]);
      }
    }

    return fld;
  }

  public static final int writeDependentData(BinaryWriter writer,
                                             FunctionType ft, Set set,
                                             FieldImpl fld)
    throws IOException
  {
    return processDependentData(writer, ft, set, fld);
  }

  public static final void write(BinaryWriter writer, FunctionType ft,
                                 Set set, FieldImpl fld, Object token)
    throws IOException
  {
    final int objLen = writeDependentData(writer, ft, set, fld);

    // if we only want to write dependent data, we're done
    if (token == SAVE_DEPEND) {
      return;
    }

    byte dataType;
    if (fld.getClass().equals(FieldImpl.class)) {
      dataType = DATA_FIELD;
    } else {
if(DEBUG_WR_DATA)System.err.println("wrFldI: punt "+fld.getClass().getName());
      BinaryUnknown.write(writer, fld, token);
      return;
    }

    int typeIndex = writer.getTypeCache().getIndex(ft);
    if (typeIndex < 0) {
      throw new IOException("FunctionType " + ft + " not cached");
    }

    DataOutputStream file = writer.getOutputStream();

if(DEBUG_WR_DATA)System.err.println("wrFldI: OBJ_DATA (" + OBJ_DATA + ")");
    file.writeByte(OBJ_DATA);
if(DEBUG_WR_DATA)System.err.println("wrFldI: objLen (" + objLen + ")");
    file.writeInt(objLen);
if(DEBUG_WR_DATA)System.err.println("wrFldI: DATA_FIELD (" + dataType + ")");
    file.writeByte(dataType);

if(DEBUG_WR_DATA)System.err.println("wrFldI: type index (" + typeIndex + ")");
    file.writeInt(typeIndex);

    if (set != null) {
if(DEBUG_WR_DATA)System.err.println("wrFldI: FLD_SET (" + FLD_SET + ")");
      file.writeByte(FLD_SET);
      BinaryGeneric.write(writer, set, token);
    }

    final int numSamples = (fld.isMissing() ? 0 : fld.getLength());
    if (numSamples > 0) {
if(DEBUG_WR_DATA)System.err.println("wrFldI: FLD_DATA_SAMPLES (" + FLD_DATA_SAMPLES + ")");
      file.writeByte(FLD_DATA_SAMPLES);
if(DEBUG_WR_DATA)System.err.println("wrFldI: numSamples (" + numSamples + ")");
      file.writeInt(numSamples);
      for (int i = 0; i < numSamples; i++) {
        DataImpl sample;
        try {
          sample = (DataImpl )fld.getSample(i);
        } catch (VisADException ve) {
          writer.getOutputStream().writeByte(DATA_NONE);
          continue;
        }

        BinaryGeneric.write(writer, sample, token);
      }
    }

if(DEBUG_WR_DATA)System.err.println("wrFldI: FLD_END (" + FLD_END + ")");
    file.writeByte(FLD_END);
  }
}