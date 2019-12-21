package util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FileSystemUtilities {

    public static List<byte[]> refactorBlocks(byte[] contents, int sizeOfFirstBlock) {
        List<byte[]> blocks = new ArrayList<>();
        int size = contents.length;
        int k = 0;

        //System.out.println("Size of first block : " + sizeOfFirstBlock);
        if(sizeOfFirstBlock % Constants.BLOCK_SIZE != 0) {
            byte firstBlock[] = new byte[sizeOfFirstBlock];
            for (int i = 0; i < Math.min(sizeOfFirstBlock, size); i++) {
                firstBlock[i] = contents[k++];
            }
            blocks.add(firstBlock);
        }

        byte currentBlock[] = new byte[Constants.BLOCK_SIZE];
        int currentBlockIndex = 0;
        while(k < size) {
            currentBlock[currentBlockIndex++] = contents[k++];
            if(currentBlockIndex % Constants.BLOCK_SIZE == 0) {
                blocks.add(currentBlock);
                currentBlockIndex = 0;
                currentBlock = new byte[Constants.BLOCK_SIZE];
            }
        }

        if(currentBlockIndex != 0) {
            blocks.add(Arrays.copyOfRange(currentBlock, 0, currentBlockIndex));
        }

        return blocks;
    }
}
