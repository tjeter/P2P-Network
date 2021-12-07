import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class FileHandler 
{
    public static final String ORIGINAL_FILE_PATH = "./FileToShare/";
    public static final String ORIGINAL_FILE_NAME = ConfigReader.getFileName();
    
    //Updated in init()
    public static volatile String PIECE_FILE_PREFIX = ""; 

    public static byte[] getFilePiece(int pieceIndex) 
    {
    	byte[] piece = new byte[0];
        File pieceFile = new File(PIECE_FILE_PREFIX + pieceIndex);
        if(!pieceFile.exists()) 
        {
            throw new RuntimeException("Piece file not found even though it was requested");
        }

        try 
        {
            piece = Files.readAllBytes(pieceFile.toPath());
        } 
        catch(IOException e) 
        {
            e.printStackTrace();
        }
        return piece;
    }

    public static void init(int clientID, boolean hasCompleteFile) 
    {
        PIECE_FILE_PREFIX = "./peer_" + clientID + "/";

        File dir = new File(PIECE_FILE_PREFIX);
        if(!dir.exists())
        {
            dir.mkdir();
        }
        for(File file: dir.listFiles()) 
        {
            if(!file.isDirectory()) 
            {
                //Delete any piece files leftover from a previous run
                file.delete();
            }
        }

        if(hasCompleteFile) 
        {
            System.out.println(Logger.getTimestamp() + ": STARTING WITH WHOLE FILE");
            initializePieceMapFromCompleteFile();
            Bitfield.selfStartsWithFile();
        }
    }

    private static synchronized void initializePieceMapFromCompleteFile() 
    {
        File file = new File(ORIGINAL_FILE_PATH+ORIGINAL_FILE_NAME);
        if(!file.exists()) 
        {
            System.out.println(Logger.getTimestamp() + ": FATAL: System cannot find file that the client should start with");
            return;
        }
        
        try 
        {
            byte[] fileBytes = Files.readAllBytes(file.toPath());
            System.out.println(Logger.getTimestamp() + ": Given file is " + fileBytes.length + " bytes long");
            int pieceSize = ConfigReader.getPieceSize();

            for(int i = 0; i < fileBytes.length; i+= pieceSize) 
            {
                int pieceIndex = i / pieceSize;
                int pieceBytesEndIndex = i + pieceSize;
                if(pieceBytesEndIndex > fileBytes.length)
                {
                    pieceBytesEndIndex = fileBytes.length;
                }
                storePiece(pieceIndex, Arrays.copyOfRange(fileBytes, i, pieceBytesEndIndex));
            }
        }
        catch(IOException ioException) 
        {
            throw new RuntimeException("Something wrong with file path probably in FileHandler\n" + ioException.getMessage());
        }
    }

    public static synchronized boolean combinePiecesIntoCompleteFile() 
    {
        Logger.logFullDownloadComplete();
        System.out.println(Logger.getTimestamp() + ": ALL PIECES RECEIVED! Attempting to combine them into file...");

        FileOutputStream fileOutputStream = null;

        try 
        {
            fileOutputStream = new FileOutputStream(new File(PIECE_FILE_PREFIX + "NEW_" + ORIGINAL_FILE_NAME));

            int maxIndex = Bitfield.calculatePieceAmt() - 1;
            
            for(int i = 0; i < maxIndex; i++) 
            {
                if(!havePieceFile(i)) 
                {
                    System.out.println(Logger.getTimestamp() + ": Tried to combine all piece into complete file, but there was a missing piece at index: " + i);
                    return false;
                }
                fileOutputStream.write(getFilePiece(i));
            }
        }
        catch(Exception e) 
        {
            throw new RuntimeException("Something wrong outputting the file\n" + e.getMessage());
        }
        finally 
        {
            try 
            {
                fileOutputStream.close();
            }
            catch(IOException e) 
            {
                throw new RuntimeException("Problem closing file output stream\n" + e.getMessage());
            }
        }
        System.out.println(Logger.getTimestamp() + ": FILE RECOMBINATION COMPLETE!");
        return true;
    }

    public static synchronized void storePiece(int pieceIndex, byte[] piece) 
    {
        File pieceFile = new File(PIECE_FILE_PREFIX + pieceIndex);

        if(pieceFile.exists()) 
        {
            System.out.println("Trying to store a piece that we already have");
            return;
        }

        try 
        {
            pieceFile.createNewFile();
            Files.write(pieceFile.toPath(), piece);
        } 
        catch(IOException e) 
        {
            e.printStackTrace();
        }
    }

    public static synchronized boolean havePieceFile(int pieceIndex) 
    {
        File pieceFile = new File(PIECE_FILE_PREFIX + pieceIndex);
        return pieceFile.exists();
    }
}