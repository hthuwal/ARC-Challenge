package search;

import java.util.ArrayList;
import java.security.InvalidParameterException;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;
import utils.*;
import utils.Config;

/**
 * Bidirectional Search algorithm based on the VLDB 2005 paper titled
 * "Bidirectional Expansion For Keyword Search on Graph Databases" by V.
 * Kacholia et. al.
 * 
 * @author Madhulika Mohanty (madhulikam@cse.iitd.ac.in)
 *
 */
public class BidirSearch implements Iterator<MyTreeResult> {

	public static MyGraph dg = new MyGraph(Config.graphFileName);
	String query;

	PriorityQueue<NodeActVals> Qin; // PriorityQueue for the incoming iterator.
	PriorityQueue<NodeActVals> Qout; // PriorityQueue for the outgoing iterator.
	Set<Node> Xin; // Set of nodes already popped from Qin.
	Set<Node> Xout; // Set of nodes already popped from Qout.
	static final Integer DMAX = new Integer(2); // Maximum depth to search up to.
	ArrayList<HashMap<Node, Float>> activationValues; // HashMap of activation values of each node per keyword indexed
														// by the list. The list length=#keywords.
	HashMap<Node, Integer> depthVals; // The depth at which a node was discovered.
	ArrayList<HashMap<Node, Node>> SP; // HashMap of next node to follow from each node to reach a keyword node indexed
										// by the list. The list length=#keywords.
	ArrayList<HashMap<Node, Integer>> distValues; // HashMap of shortest distance from each node to reach a keyword node
													// indexed by the list. The list length=#keywords.
	List<List<Node>> keywordSet; // #keyword length list of list of keyword nodes.
	PriorityQueue<MyTreeResult> resultHeap; // The result trees sorted by score.
	Comparator<NodeActVals> comparator; // Comparator for node activation values for use by Qin and Qout.
	HashMap<Node, ArrayList<Node>> reachedAncestors; // Map of node to its reached ancestors.
	Comparator<MyTreeResult> comparatorRes; // Answer tree comparator for use by resultHeap.
	boolean is_emitted; // Does the result heap have an answer?
	int countOfAnswers; // The number of answers seen.
	Set<MyTreeResult> seenAnswers; // Set of answers seen already.

	long runtime; // Time to run the bidirectional search.

	static final Integer ANY_PREDICATE = 0; // Constant used to query for triples with any predicate.

	public BidirSearch(String q) {
		this.runtime = (new Date()).getTime();
		this.comparator = new NodeActComparator();
		this.query = q;
		this.Qin = new PriorityQueue<NodeActVals>(1000, comparator);
		this.Qout = new PriorityQueue<NodeActVals>(1000, comparator);
		this.Xin = new HashSet<Node>();
		this.Xout = new HashSet<Node>();
		this.activationValues = new ArrayList<HashMap<Node, Float>>();
		this.depthVals = new HashMap<Node, Integer>();
		this.SP = new ArrayList<HashMap<Node, Node>>();
		this.distValues = new ArrayList<HashMap<Node, Integer>>();
		this.keywordSet = new ArrayList<List<Node>>();
		this.reachedAncestors = new HashMap<Node, ArrayList<Node>>();
		this.comparatorRes = new TreeResultComparator();
		this.resultHeap = new PriorityQueue<MyTreeResult>(1000, comparatorRes);
		this.seenAnswers = new HashSet<MyTreeResult>();
		this.is_emitted = false;
		this.countOfAnswers = 0;
		init();
	}

	/**
	 * Gets the list of nodes matching queried keywords.
	 * 
	 * @return List of nodes for each keyword.
	 */
	public List<List<Node>> getKeywordSet() {
		if (dg == null) {
			System.err.println("DatasetGraph is not initialized!!");
			System.exit(0);
		}
		List<List<Node>> queryNodes = new ArrayList<List<Node>>();
		String[] entities = query.split(" ");
		for (String entity : entities) {
			HashMap<Node, Integer> newHM = new HashMap<Node, Integer>();
			this.SP.add(new HashMap<Node, Node>());
			ArrayList<Node> listOfNodes = new ArrayList<Node>();
			entity = "<" + entity.toLowerCase() + ">"; // toLowerCase() to ensure compatibility. Angular brackets to
														// match typical RDF node URIs.
			if (NametoNumMap.nameToNum.containsKey(entity)) { // Exact matching of node labels. May change to check
																// partial matches.
				Node val = new Node(NametoNumMap.nameToNum.get(entity));
				listOfNodes.add(val);
				this.depthVals.put(val, 0);
				newHM.put(val, 0);

			} else
				throw new IllegalArgumentException("Did not find existing id for queried node:" + entity);
			queryNodes.add(listOfNodes);
			this.distValues.add(newHM);
		}
		this.keywordSet = queryNodes;
		return queryNodes;

	}

	/**
	 * Bidirectional expansion search initialization.
	 */
	public void init() {
		Integer defaultNodeActVal = new Integer(10);

		List<List<Node>> S = getKeywordSet();

		// Initialise Qin with S.
		System.out.println("................Starting bidir search................");
		for (int i = 0; i < S.size(); i++) {
			List<Node> temp = S.get(i);
			this.activationValues.add(new HashMap<Node, Float>());
			for (int j = 0; j < temp.size(); j++) {
				this.Qin.add(new NodeActVals(temp.get(j), new Float(defaultNodeActVal / temp.size())));
				this.activationValues.get(i).put(temp.get(j), new Float(defaultNodeActVal / temp.size()));
				depthVals.put(temp.get(j), 0); // depth of each keyword node = 0.

				/*
				 * Initialize dist vals. Is getting done in getKeywordSet(). for(int
				 * k=0;k<S.size();k++) { HashMap<Node,Integer> distVals = new
				 * HashMap<Node,Integer>(); if(k==i) distVals.put(temp.get(j), 0); else
				 * distVals.put(temp.get(j), Integer.MAX_VALUE);
				 * this.distValues.add(k,distVals); }
				 */
			}
		}
	}

	/**
	 * Insert or update a node in the outgoing iterator.
	 * 
	 * @param va    The node to be inserted/updated.
	 * @param depth The depth at which it was discovered. If it is -1, then it is
	 *              being inserted by the incoming operator and need not be updated
	 *              now.
	 */
	private void insertOrUpdateQout(NodeActVals va, int depth) {
		NodeActVals tmp = presentIn(va.getNode(), this.Qout);

		if (tmp.getNode().equals(va.getNode())) // If the node exists in the queue already
		{
			if (tmp.getActivationval() == va.getActivationval()) // Check if its activation value has changed
				return;
			else {
				this.Qout.remove(tmp);
				this.Qout.add(va);
				// Depthv=Depthu+1
				if (depth != -1)
					depthVals.put(va.getNode(), depth);
			}
		} else { // Add a new node to the queue.
			this.Qout.add(va);
			// Depthv=Depthu+1
			if (depth != -1)
				depthVals.put(va.getNode(), depth);
		}
	}

	/**
	 * Insert or update a node in the incoming iterator.
	 * 
	 * @param ua    The node to be inserted/updated.
	 * @param depth Depth at which the node was found.
	 */
	private void insertOrUpdateQin(NodeActVals ua, int depth) {
		NodeActVals tmp = presentIn(ua.getNode(), this.Qin);

		if (tmp.getNode().equals(ua.getNode())) // If the node exists in the queue already
		{
			if (tmp.getActivationval() == ua.getActivationval()) // Check if its activation value has changed
				return;
			else {
				this.Qin.remove(tmp);
				this.Qin.add(ua);
				// Depthu=Depthv+1
				depthVals.put(ua.getNode(), depth);
			}
		} else { // Add a new node to the queue.
			this.Qin.add(ua);
			// Depthu=Depthv+1
			depthVals.put(ua.getNode(), depth);
		}
	}

	/**
	 * Computes activation of a node based on its neighbours.
	 * 
	 * @param u The node whose activation is to be computed.
	 * @return The activation value.
	 */
	private Float computeActivation(Node u) {// , NodeActVals va,int direction) {
		// Compute activation of u from va where direction =0 imples edge u->v and 1
		// imples v->u
		Float uVal = new Float(0.0);
		for (int c = 0; c < this.activationValues.size(); c++) {
			if (this.activationValues.get(c).get(u) != null)
				uVal += this.activationValues.get(c).get(u);
		}
		return uVal;
	}

	/**
	 * Detects if the egde u-->v constitutes a cycle. It checks if {@link Node} v
	 * exists in the ancestors of {@Node} u recursively.
	 * 
	 * @param u Source node
	 * @param v Target node
	 * @return True if a cycle exists, false otherwise.
	 */
	private boolean detectCycle(Node u, Node v) {
		if (!this.reachedAncestors.containsKey(u))
			return false;
		else {
			ArrayList<Node> arrTmp = this.reachedAncestors.get(u);
			boolean val = false;
			for (int i = 0; i < arrTmp.size(); i++) {
				if (arrTmp.get(i).equals(v))
					return true;
				val = val || detectCycle(arrTmp.get(i), v);
			}
			return val;
		}

	}

	/**
	 * Explore the triple i.e., look for keyword answers.
	 * 
	 * @param q       The edge to be explored.
	 * @param inOrOut 0 for incoming iterator, 1 for outgoing iterator.
	 * @return True if explored, false if a cycle is detected.
	 */
	private boolean exploreEdge(Triple q, int inOrOut) {

		if (inOrOut > 1)
			throw new InvalidParameterException("Unexpected value for inOrOut in exploreEdge(). "
					+ "Pass 0 for incoming iterator, 1 for outgoing iterator.");
		Node u = new Node(q.getSubject());
		Node v = new Node(q.getObject());
		ArrayList<Node> tmplist = this.reachedAncestors.get(v);
		if (tmplist == null) {
			if (!detectCycle(u, v)) {
				ArrayList<Node> arr = new ArrayList<Node>();
				arr.add(u);
				this.reachedAncestors.put(v, arr);
			} else
				return false;
		} else {
			if (!detectCycle(u, v)) {
				tmplist.add(u);
				this.reachedAncestors.put(v, tmplist);
			} else
				return false;
		}

		String[] querySplit = query.split(" ");
		for (int i = 0; i < querySplit.length; i++) {
			HashMap<Node, Integer> tmp = this.distValues.get(i);
			Integer tmm = tmp.get(u);
			if (tmp.get(v) != null) {// TODO is null for outgoing iterator almost always, as the target node is still
										// unexplored.

				// If no entry exists for u, set its value to Integer.MAX_VALUE to be checked
				// and set to correct val in the following check.
				if (tmm == null) {
					tmm = Integer.MAX_VALUE;
				}

				if (tmm > (tmp.get(v) + 1)) { // If v offers a better path to keyword i

					// SP(u,i)=v;
					HashMap<Node, Node> ht = this.SP.get(i);
					ht.remove(u);
					ht.put(u, v);// Works in place.

					// Dist(u,i)=Dist(v,i)+1;
					Integer tm = tmp.remove(u);
					tm = tmp.get(v) + 1;
					tmp.put(u, tm);

					// updatePriority(u,v,i); // Madhulika:Not required here, I feel.
					Attach(u, i);
					if (isComplete(u)) {
						emitResult(u);
					}
				}
			}

			// if v spreads more activation to u from t_i then
			// update a(u,i) with new activation value. Vice versa for outgoing iterator.
			Node toActivate;
			if (inOrOut == 0)
				toActivate = u;
			else
				toActivate = v;
			Float newAct = getActivation(u, v, i, inOrOut);
			HashMap<Node, Float> tmp1 = this.activationValues.get(i);
			Float oldAct = new Float(0.0);
			if (tmp1.containsKey(toActivate)) {
				oldAct = tmp1.get(toActivate);
			} else {
				tmp1.put(toActivate, oldAct);
			}
			// oldit will be 0.0 if there is no activation to 'u' already.
			if (newAct > oldAct) {
				if (tmp1.containsKey(toActivate))
					oldAct = tmp1.remove(toActivate);
				tmp1.put(toActivate, newAct);
				activate(toActivate, i, oldAct);
			}
		}
		return true;
	}

	/**
	 * Propagates changes in distances to a keyword to all ancestors of the node.
	 * 
	 * @param u Initial node
	 * @param i Keyword offset
	 */
	private void Attach(Node u, int i) {
		// Paper says we need to update priority of u in Qin. I feel it is not required
		// as we have not updated activation values yet.

		// propagating change in cost dist (v,k) to all ancestors in best first manner.
		// propagateChangeInDistance(u,i);
		ArrayList<Node> ancestors = this.reachedAncestors.get(u);
		if (ancestors == null || ancestors.size() == 0)
			return;
		for (int c = 0; c < ancestors.size(); c++) {
			Node ancestor = ancestors.get(c);
			HashMap<Node, Integer> tmp = this.distValues.get(i);
			Integer tU = tmp.get(u);
			Integer tAncestor = tmp.remove(ancestor);
			tAncestor = tU + 1;
			tmp.put(ancestor, tAncestor);
			HashMap<Node, Node> tmpSP = this.SP.get(i);
			tmpSP.remove(ancestor);
			tmpSP.put(ancestor, u);
			Attach(ancestor, i);
		}
	}

	/**
	 * Propagates changes in activation and updation to the input queue based on it.
	 * 
	 * @param u     Node whose activation has changed.
	 * @param i     keyword offset.
	 * @param oldit Old activation value.
	 */
	private void activate(Node u, int i, Float oldit) { // Note that a change in activation values may change the
														// priority queue
		// Hence, re-check is required each time.
		NodeActVals tmp = presentIn(u, this.Qin);
		if (tmp.getNode().equals(u)) {
			this.Qin.remove(tmp);
			tmp.setActivationVal(tmp.getActivationval() - oldit + this.activationValues.get(i).get(u));
			this.Qin.add(tmp);
		}
		// Propagate change in Activation value to all reached ancestors in best first
		// manner.
		ArrayList<Node> ancestors = this.reachedAncestors.get(u);
		if (ancestors == null || ancestors.size() == 0)
			return;
		for (int c = 0; c < ancestors.size(); c++) {
			Node ancestor = ancestors.get(c);
			// change values
			Float uVal = this.activationValues.get(i).get(u);
			Float oldVal = this.activationValues.get(i).get(ancestor);
			// Calculate size of edges
			Iterator<Triple> it;
			it = BidirSearch.dg.find(Node.ANY, ANY_PREDICATE, u);
			int size = 0;
			while (it.hasNext()) {
				size = size + 1;
				it.next();
			}
			Float newVal = (uVal / 2) * (1 / size);
			if (newVal > oldVal) {
				this.activationValues.get(i).remove(ancestor);
				this.activationValues.get(i).put(ancestor, newVal);
				activate(ancestor, i, oldVal);
			}
		}
		return;
	}

	/**
	 * Check if a node exists in the queue.
	 * 
	 * @param u   Node to be checked.
	 * @param qin Queue to be checked.
	 * @return The matched node.
	 */
	private NodeActVals presentIn(Node u, PriorityQueue<NodeActVals> qin) {

		Iterator<NodeActVals> iter = qin.iterator();
		while (iter.hasNext()) {
			NodeActVals tmp = iter.next();
			if (tmp.getNode().equals(u))
				return tmp;
		}
		return new NodeActVals();
	}

	/**
	 * Get activation spread by the iterator.
	 * 
	 * @param u       Source node
	 * @param v       Target node
	 * @param i       Keyword offset
	 * @param inOrOut 0 for incoming iterator, 1 for outgoing iterator.
	 * @return The activation value spread by the iterator.
	 */
	private Float getActivation(Node u, Node v, int i, int inOrOut) {
		Iterator<Triple> it;
		Node toCheck;
		if (inOrOut == 0) {
			it = BidirSearch.dg.find(Node.ANY, ANY_PREDICATE, v);
			toCheck = v;
		} else {
			it = BidirSearch.dg.find(u, ANY_PREDICATE, Node.ANY);
			toCheck = u;
		}
		int size = 0;
		while (it.hasNext()) {
			size = size + 1;
			it.next();
		}

		Float toCheckVal = new Float(0.0);
		if (this.activationValues.get(i).containsKey(toCheck))
			toCheckVal = this.activationValues.get(i).get(toCheck);
		else
			this.activationValues.get(i).put(toCheck, toCheckVal);

		return (new Float(0.5)) * (toCheckVal / size); // 0.5 attenuation factor, rest 0.5 divided in inverse ratio of
														// the number of edges.
	}

	/**
	 * Adds a result to the output heap.
	 * 
	 * @param n Root node of the result.
	 */
	private void emitResult(Node n) {

		// Construct result tree using SP values.
		MyTreeResult res = new MyTreeResult();
		res.setRoot(n);
		Integer numEdges = 0;
		for (int i = 0; i < this.keywordSet.size(); i++) {
			// TODO Fix this. This is redundant with the while loop.
			if (isTerminalNode(n, i))
				continue;
			Node nxt = this.SP.get(i).get(n);
			Node prev;
			numEdges++;
			Iterator<Triple> iter = BidirSearch.dg.find(n, ANY_PREDICATE, nxt);
			if (iter.hasNext()) { // Changed while to if to take only one triple.
				Triple q = iter.next();
				res.addEdge(new Node(q.getSubject()), q.getPredicate(), new Node(q.getObject()));
			}

			prev = nxt;
			while (!isTerminalNode(nxt, i)) {
				numEdges++;
				nxt = this.SP.get(i).get(prev);
				Iterator<Triple> iter1 = BidirSearch.dg.find(prev, ANY_PREDICATE, nxt);
				if (iter1.hasNext()) {// Changed while to if to take only one triple.
					Triple q = iter1.next();
					res.addEdge(new Node(q.getSubject()), q.getPredicate(), new Node(q.getObject()));
				}
				prev = nxt;
			}
		}
		res.setNumEdges(numEdges);
		checkAndAddToResultHeap(res);
	}

	/**
	 * Adds a result to the heap if not duplicate.
	 * 
	 * @param res The {@link MyTreeResult} to be added to the heap.
	 */
	private void checkAndAddToResultHeap(MyTreeResult res) {
		if (!redundant(res)) {
			this.resultHeap.add(res);
			this.seenAnswers.add(res);
			is_emitted = true;
		}
	}

	/**
	 * Checks if a result is duplicate. Required to remove duplicate answers with
	 * different roots. Refer section 4.2.3 of the paper.
	 * 
	 * @param res The {@link MyTreeResult} to be checked.
	 * @return {@code true} if duplicate, {@code false} otherwise.
	 */
	private boolean redundant(MyTreeResult res) {
		Iterator<MyTreeResult> iter = this.seenAnswers.iterator();
		String resString = res.toString();
		while (iter.hasNext()) {
			MyTreeResult tmp = iter.next();
			if (resString.equals(tmp.toString())) // Compare string notation of each tree.
				return true;
		}
		return false;
	}

	/**
	 * Check if a node has keyword i
	 * 
	 * @param nxt Node
	 * @param i   keyword offset
	 * @return true if the node is a terminal node for keyword i
	 */
	private boolean isTerminalNode(Node nxt, int i) {
		List<Node> tmp = this.keywordSet.get(i);
		if (tmp.contains(nxt)) {
			return true;
		}
		return false;
	}

	/**
	 * Checks if a node has paths to all keyword nodes.
	 * 
	 * @param n Node to be checked.
	 * @return True if complete, false otherwise.
	 */
	private boolean isComplete(Node n) {
		for (int i = 0; i < this.distValues.size(); i++) {
			HashMap<Node, Integer> tmp = this.distValues.get(i);
			if (!tmp.containsKey(n)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean hasNext() {
		if (this.resultHeap.size() > 0)
			return true;
		is_emitted = false;
		while (Qin.size() != 0 || Qout.size() != 0) {

			if (is_emitted)
				break;

			NodeActVals maxQinNA = this.Qin.peek();
			NodeActVals maxQoutNA = this.Qout.peek();
			Node maxQin = null, maxQout = null;
			Float QinA = new Float(-6.0), QoutA = new Float(-6.0);
			if (maxQinNA != null) {
				maxQin = maxQinNA.getNode();
				QinA = maxQinNA.getActivationval();
			}
			if (maxQoutNA != null) {
				maxQout = maxQoutNA.getNode();
				QoutA = maxQoutNA.getActivationval();
			}
			if (QinA > QoutA) { // Qin has higher activation.

				// pop best v in Qin; insert in Xin
				Qin.remove(maxQinNA);
				Xin.add(maxQinNA.getNode());

				// if IS_COMPLETE(v) then EMIT(v)
				if (isComplete(maxQin))
					emitResult(maxQin);

				if (depthVals.get(maxQin) <= DMAX) { // if depth(v)<Dmax then
					// For every incoming edge from u to v, exploreedge(u,v).
					// for every u belongs to incoming[v]
					// EXPLOREEDGE(u,v)
					Iterator<Triple> itt = dg.find(Node.ANY, ANY_PREDICATE, maxQin);
					while (itt.hasNext()) {
						Triple q = itt.next();
						boolean val = exploreEdge(q, 0);
						if (!val) // Why is the check required? exploreEdge(q) returns true only when the edge
									// does not lead to a cycle.
							continue;

						Node u = new Node(q.getSubject());
						Node v = new Node(q.getObject()); // maxQin here.

						// if u does not belong in Xin, insert in Qin
						if (!Xin.contains(u)) {
							// Compute activation below
							NodeActVals ua = new NodeActVals();
							ua.setNode(u);
							ua.setActivationVal(computeActivation(u));// ,maxQinNA,0));
							if (depthVals.get(u) == null)
								depthVals.put(u, Integer.MAX_VALUE);
							insertOrUpdateQin(ua, Math.min(depthVals.get(u), depthVals.get(v) + 1));

						}
					}

					// if v doesnt belong Xout insert in Qout
					if (!Xout.contains(maxQinNA.getNode())) {
						insertOrUpdateQout(maxQinNA, -1);
					}
				}
			}

			else // Qout has higher activation
			{

				// pop best u in Qout; insert in Xout
				Qout.remove(maxQoutNA);
				Xout.add(maxQoutNA.getNode());

				// if IS_COMPLETE(u) then EMIT(u)
				if (isComplete(maxQout))
					emitResult(maxQout);

				if (depthVals.get(maxQout) <= DMAX) {

					try {
						Iterator<Triple> itt = dg.find(maxQout, ANY_PREDICATE, Node.ANY);
						while (itt.hasNext()) {

							Triple q = itt.next();

							boolean val = exploreEdge(q, 1);
							if (!val) // Why is the check required? exploreEdge(q) returns true only when the edge
										// does not lead to a cycle.
								continue;
							Node u = new Node(q.getSubject()); // maxQout here.
							Node v = new Node(q.getObject());
							if (!Xout.contains(v)) {
								NodeActVals va = new NodeActVals();
								va.setNode(v);
								va.setActivationVal(computeActivation(v));// ,maxQoutNA,1));
								if (depthVals.get(v) == null)
									depthVals.put(v, Integer.MAX_VALUE);
								insertOrUpdateQout(va, Math.min(depthVals.get(v), depthVals.get(u) + 1));
							}

						}
					} catch (Exception e) {
						System.out.println("Exception!!!!");
						e.printStackTrace();
					}
				}
			}
		}
		return is_emitted;
	}

	@Override
	public MyTreeResult next() {
		countOfAnswers++;
		MyTreeResult mtr = this.resultHeap.poll();
		return mtr;
	}

	public void close() {
		long timeNow = (new Date()).getTime();
		this.runtime = timeNow - this.runtime;
		System.out.println("Bidir search concluded in (time in ms):" + this.runtime);
		System.out.println("Bidir search concluded with #results found:" + this.countOfAnswers);
		System.out.println("Nodes explored:" + this.Xin);
		System.out.println("#Nodes explored:" + this.Xin.size());
		System.out.println("................Bidir search closed................");
	}

}
