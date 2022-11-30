import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.security.PublicKey;


public class TxHandler {

	
	/**
	 * 
	 *I acknowledge that I am aware of the academic integrity guidelines of this course, and that I worked on this assignment independently without any unauthorized help - Omar Rehan
	 */
	 
    private UTXOPool utxoPool;

    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. 
     */
    public TxHandler(UTXOPool utxoPool) {
        this.utxoPool = new UTXOPool(utxoPool);
    }

    /**
     * @return true if:
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool, 
     * (2) the signatures on each input of {@code tx} are valid, 
     * (3) no UTXO is claimed multiple times by {@code tx},
     * (4) all of {@code tx}s output values are non-negative, and
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     *     values; and false otherwise.
     */
    public UTXOPool getUTXOPool() {
    	return this.utxoPool;
    }
    public boolean isValidTx(Transaction tx) {
    	
    	
    	
    	ArrayList<Transaction.Output> txOutputs =  tx.getOutputs();
    	ArrayList<Transaction.Input> txInputs =  tx.getInputs();
    	ArrayList<UTXO> mp = new ArrayList<>();
    	
    	double inputSum=0 ,outputSum=0 ;
    	
    	
    	for(int i=0 ;i<txInputs.size() ;i++)
    	{
    		Transaction.Input input = txInputs.get(i);
    		UTXO utxo = new UTXO(input.prevTxHash , input.outputIndex) ;
    		Transaction.Output output = this.utxoPool.getTxOutput(utxo) ;
    		
    		if(! this.utxoPool.contains(utxo))
    			return false ;
    		
    		
    		if(! Crypto.verifySignature(output.address,tx.getRawDataToSign(i),input.signature))
    			return false ;
    		
    		if(mp.contains(utxo))
    			return false ;
    		mp.add(utxo);
    		
    		inputSum += output.value ;
    		
    		
    	}
    	

    	
      	for(Transaction.Output output : txOutputs)
    	{
    		if(output.value < 0)
    			return false ;
    		outputSum += output.value ;
    	}
    	
    	
    	if(inputSum < outputSum)
    		return false ;
    	
    	
    	return true ;
    	
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
    
    	ArrayList<Transaction> valid = new ArrayList<>();
    	
    	Boolean flag = false ;
    	while(true)
    	{
    		
    		flag=false ;
    		
    		for(Transaction tx : possibleTxs)
        	{
    			if(this.isValidTx(tx))
        		{
        			flag=true ;
        			
        			
        			ArrayList<Transaction.Input> txInputs = tx.getInputs();
        			for(Transaction.Input input : txInputs)
        			{
        				UTXO utxo = new UTXO(input.prevTxHash , input.outputIndex) ;
        				this.utxoPool.removeUTXO(utxo);
        			}
        			
        			
        			ArrayList<Transaction.Output> txOutputs =  tx.getOutputs();
        			for(int i=0 ;i<txOutputs.size() ;i++)
        			{
        				Transaction.Output output = txOutputs.get(i) ;
        				UTXO utxo = new UTXO(tx.getHash() , i) ;
        				this.utxoPool.addUTXO(utxo, output);
        			}
        			
        			
        			valid.add(tx);
        		}
       
        	}
    		if(!flag)
    			break ;
    	}
    	
    	
    	Transaction list[] =  new Transaction[valid.size()] ;
    	
    	return valid.toArray(list) ;
    }
    
    
    
   

}
