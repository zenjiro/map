package route;

import java.awt.geom.GeneralPath;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

/**
 * 最短経路探索を行うクラスです。
 * @author zenjiro
 * @since 5.03
 * 2007/01/01
 */
public class Route {

	/**
	 * 辺の種類を表す列挙型です。
	 * @author zenjiro
	 * @since 5.03
	 * 2007/01/01
	 */
	public enum Category {
		/**
		 * 高速道路
		 */
		ROAD_HIGHWAY,
		/**
		 * 国道
		 */
		ROAD_KOKUDO,
		/**
		 * 県道
		 */
		ROAD_KENDO,
		/**
		 * 主要地方道
		 */
		ROAD_CHIHODO,
		/**
		 * 名前のある道路
		 */
		ROAD_MAJOR,
		/**
		 * 一般道
		 */
		ROAD_OTHER,
		/**
		 * JR新幹線
		 */
		RAILWAY_SHINKANSEN,
		/**
		 * JR在来線
		 */
		RAILWAY_JR,
		/**
		 * 私鉄
		 */
		RAILWAY_OTHER,
		/**
		 * 徒歩
		 */
		RAILWAY_WALK,
		/**
		 * 不明な辺
		 */
		UNKNOWN,
	}

	/**
	 * インスタンス
	 */
	private static Route instance;

	/**
	 * インスタンスを取得します。
	 * @return インスタンス
	 */
	public static Route getInstance() {
		if (Route.instance == null) {
			Route.instance = new Route();
		}
		return Route.instance;
	}

	/**
	 * 辺をカプセル化するクラスです。
	 * @author zenjiro
	 * @since 5.03
	 */
	private static class Edge {
		/**
		 * 頂点1
		 */
		private String node1;

		/**
		 * 頂点2
		 */
		private String node2;

		/**
		 * 辺
		 */
		private GeneralPath path;

		/**
		 * 辺の種類
		 */
		private Category category;

		/**
		 * コンストラクタです。
		 * @param node1 頂点1
		 * @param node2 頂点2
		 * @param path 辺
		 * @param category 辺の種類
		 */
		public Edge(final String node1, final String node2, final GeneralPath path, final Category category) {
			this.node1 = node1;
			this.node2 = node2;
			this.path = path;
			this.category = category;
		}
	}

	/**
	 * 頂点と値をカプセル化する比較可能なクラスです。
	 * @author zenjiro
	 * @since 5.03
	 */
	private static class Node implements Comparable<Node> {
		/**
		 * 頂点
		 */
		private String node;

		/**
		 * 値
		 */
		private double value;

		/**
		 * コンストラクタです。
		 * @param node 頂点
		 * @param value 値
		 */
		public Node(final String node, final double value) {
			this.node = node;
			this.value = value;
		}

		public int compareTo(final Node other) {
			return this.value < other.value ? 1 : (this.value > other.value ? -1 : 0);
		}
	}

	/**
	 * シングルトン用のコンストラクタです。
	 */
	private Route() {
		this.graph = new HashMap<String, Collection<Edge>>();
	}

	/**
	 * 頂点をキー、辺の一覧を値とするグラフ
	 */
	private Map<String, Collection<Edge>> graph;

	/**
	 * 始点
	 */
	private String start;

	/**
	 * 終点
	 */
	private String goal;

	/**
	 * 最短経路
	 */
	private List<GeneralPath> route;

	/**
	 * 経路探索のための辺を1本追加します。
	 * @param node1 頂点1
	 * @param node2 頂点2
	 * @param path 辺
	 * @param category 辺の種類
	 */
	public void add(final String node1, final String node2, final GeneralPath path, final Category category) {
		final Edge edge = new Edge(node1, node2, path, category);
		for (final String node : new String[] { node1, node2 }) {
			if (!this.graph.containsKey(node)) {
				this.graph.put(node, new HashSet<Edge>());
			}
			if (!this.graph.get(node).contains(edge)) {
				this.graph.get(node).add(edge);
			}
		}
	}

	/**
	 * 経路探索のための情報をクリアします。
	 */
	public void clear() {
		this.graph.clear();
	}

	/**
	 * @return 終点
	 */
	public String getGoal() {
		return this.goal;
	}

	/**
	 * @return 最短経路
	 */
	public List<GeneralPath> getRoute() {
		if (this.route.isEmpty()) {
			this.calcRoute();
		}
		return this.route;
	}

	/**
	 * 最短経路を求め、フィールドに記憶します。
	 */
	private void calcRoute() {
		this.route.clear();
		if (this.start == null || this.goal == null || !this.graph.containsKey(this.start)
				|| !this.graph.containsKey(this.goal)) {
			return;
		}
		final Set<Edge> doneEdges = new HashSet<Edge>();
		final Set<String> doneNodes = new HashSet<String>();
		final PriorityQueue<Node> queue = new PriorityQueue<Node>();
		queue.add(new Node(this.start, 0));
		while (!queue.isEmpty()) {
			final Node node = queue.poll();
			if (node.node == goal) {
				// TODO 親をたどって経路を表示する。
				break;
			}
			doneNodes.add(node.node);
			if (!graph.containsKey(node.node)) {
				continue;
			}
			for (final Edge edge : this.graph.get(node.node)) {
				doneEdges.add(edge);
				for (final String node2 : new String[] { edge.node1, edge.node2 }) {
					// TODO 値の更新は未実装。
					if (!doneEdges.contains(node2)) {
						// TODO とりあえず外接長方形の長い方を辺の長さとしてみる。
						queue.add(new Node(node2, node.value
								+ Math.max(edge.path.getBounds2D().getWidth(), edge.path.getBounds2D().getHeight())));
					}
				}
			}
		}
	}

	/**
	 * @return 始点
	 */
	public String getStart() {
		return this.start;
	}

	/**
	 * 終点を設定します。
	 * @param goal 終点
	 */
	public void setGoal(final String goal) {
		this.goal = goal;
	}

	/**
	 * 辺の種類毎に速度を設定します。
	 * @param category 辺の種類
	 * @param speed 速度[km/h]
	 */
	public void setSpeed(final Category category, final double speed) {
		// TODO 未実装
	}

	/**
	 * 始点を設定します。
	 * @param start 始点
	 */
	public void setStart(final String start) {
		this.start = start;
	}

}
