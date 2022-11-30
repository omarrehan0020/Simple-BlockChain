import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


// The BlockChain class should maintain only limited block nodes to satisfy the functionality.
// You should not have all the blocks added to the block chain in memory 
// as it would cause a memory overflow.

//“I acknowledge that I am aware of the academic integrity guidelines of this course, and that I worked on this assignment independently without any unauthorized help” - Omar Rehan


 class BlockNode{
	
	public Block block ;
	public BlockNode parent ;
	public int height = 1 ;
	UTXOPool utxoPool ;
	
	
	public BlockNode(Block b , BlockNode p , UTXOPool utxoPool) {
		
		this.block = b ;
		this.parent = p ;
		this.utxoPool = utxoPool ;
		if(p != null)
			this.height = p.height + 1 ;
		
	}
	
}
 
 
public class BlockChain {
	
    public static final int CUT_OFF_AGE = 10;
    BlockNode MaxHeightBlockNode ;
    TransactionPool transactionPool ;
    HashMap<ByteArrayWrapper , BlockNode> mp ;
    
    

    /**
     * create an empty blockchain with just a genesis block. Assume {@code genesisBlock} is a valid
     * block
     */
    public BlockChain(Block genesisBlock) {
    	
    	mp = new HashMap<ByteArrayWrapper , BlockNode>();
    	
    	this.transactionPool = new TransactionPool() ;
    	
    	UTXOPool utxoPool = new UTXOPool();
    	
    	Transaction coinbase = genesisBlock.getCoinbase();
    	
        ArrayList<Transaction.Output> outTxs = coinbase.getOutputs() ;
        for (int i = 0; i < outTxs.size() ; i++) {
            Transaction.Output output = outTxs.get(i);
            UTXO utxo = new UTXO(coinbase.getHash(), i);
            utxoPool.addUTXO(utxo, output);
        }
    	
    	BlockNode bn = new BlockNode(genesisBlock , null,utxoPool) ;
    	    	
    	mp.put(new ByteArrayWrapper(genesisBlock.getHash()), bn);
    	this.MaxHeightBlockNode = bn ;
    	
    }

    
    /** Get the maximum height block */
    public Block getMaxHeightBlock() {
        return this.MaxHeightBlockNode.block ;
    }

    /** Get the UTXOPool for mining a new block on top of max height block */
    public UTXOPool getMaxHeightUTXOPool() {
        return this.MaxHeightBlockNode.utxoPool ;
    }

    /** Get the transaction pool to mine a new block */
    public TransactionPool getTransactionPool() {
        return this.transactionPool ;
    }

    /**
     * Add {@code block} to the blockchain if it is valid. For validity, all transactions should be
     * valid and block should be at {@code height > (maxHeight - CUT_OFF_AGE)}, where maxHeight is 
     * the current height of the blockchain.
	 * <p>
	 * Assume the Genesis block is at height 1.
     * For example, you can try creating a new block over the genesis block (i.e. create a block at 
	 * height 2) if the current blockchain height is less than or equal to CUT_OFF_AGE + 1. As soon as
	 * the current blockchain height exceeds CUT_OFF_AGE + 1, you cannot create a new block at height 2.
     * 
     * @return true if block is successfully added
     */
    public boolean addBlock(Block block) {
    	
    	
        if(block == null || block.getPrevBlockHash() == null)
        	return false;
        
        BlockNode parent = mp.get(new ByteArrayWrapper(block.getPrevBlockHash()));
        if(parent == null)
        	return false ;
        
        int h = parent.height+1 ;
        if(h <= this.MaxHeightBlockNode.height - CUT_OFF_AGE)
        	return false ;
        
        UTXOPool utxoPool = parent.utxoPool ;
        
        TxHandler handler = new TxHandler(utxoPool) ;
        Transaction[] txs = block.getTransactions().toArray(new Transaction[0]) ;
        Transaction valid[] = handler.handleTxs(txs);
        
        if(valid.length != txs.length)
        	return false ;
        
        
        utxoPool = handler.getUTXOPool();
        
        Transaction coinbase = block.getCoinbase();
        
        ArrayList<Transaction.Output> outoutTxs = coinbase.getOutputs() ;
        for (int i = 0; i < outoutTxs.size() ; i++) {
            Transaction.Output output = outoutTxs.get(i);
            UTXO utxo = new UTXO(coinbase.getHash(), i);
            utxoPool.addUTXO(utxo, output);
        }

        for(Transaction tx : txs)
        	this.transactionPool.removeTransaction(tx.getHash()) ;
        
        BlockNode bn = new BlockNode(block , parent , utxoPool);

        if (h > MaxHeightBlockNode.height) {
        	MaxHeightBlockNode = bn;
        }
        
        
    	mp.put(new ByteArrayWrapper(block.getHash()),bn) ;
    	
    	if(this.MaxHeightBlockNode.height - CUT_OFF_AGE > 1)
    		removeOldBlocks(this.MaxHeightBlockNode.height - CUT_OFF_AGE) ;

        
        return true ;
    }

    /** Add a transaction to the transaction pool */
    public void addTransaction(Transaction tx) {
        this.transactionPool.addTransaction(tx);
    }
    
    private void removeOldBlocks(int x) {
    	
    	Map<ByteArrayWrapper, BlockNode> dummy = new HashMap<ByteArrayWrapper, BlockNode>();
        dummy.putAll(this.mp);
        
        for (Iterator<ByteArrayWrapper> keys = dummy.keySet().iterator(); keys.hasNext();) {
        	 ByteArrayWrapper key = keys.next();
             BlockNode block = mp.get(key);
	          if(block.height < x)
	       	 	mp.remove(key);
        }
    	
		 
    	return ;
    }
}