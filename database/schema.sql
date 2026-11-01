-- DevClash Database Schema & Initial Question Bank
CREATE DATABASE IF NOT EXISTS devclash_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE devclash_db;

-- 1. Categories
CREATE TABLE IF NOT EXISTS categories (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    slug VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    icon_name VARCHAR(50) DEFAULT 'Terminal',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 2. Questions
CREATE TABLE IF NOT EXISTS questions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    category_id INT NOT NULL,
    difficulty ENUM('EASY', 'MEDIUM', 'HARD') NOT NULL DEFAULT 'MEDIUM',
    question_text TEXT NOT NULL,
    code_snippet TEXT NULL,
    option_a VARCHAR(255) NOT NULL,
    option_b VARCHAR(255) NOT NULL,
    option_c VARCHAR(255) NOT NULL,
    option_d VARCHAR(255) NOT NULL,
    correct_option ENUM('A', 'B', 'C', 'D') NOT NULL,
    explanation TEXT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_questions_category FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE,
    INDEX idx_category_difficulty (category_id, difficulty)
) ENGINE=InnoDB;

-- 3. Historical Matches
CREATE TABLE IF NOT EXISTS game_matches (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    match_uuid VARCHAR(64) NOT NULL UNIQUE,
    room_code VARCHAR(10) NOT NULL,
    game_mode ENUM('CLASSIC', 'SPEED_RUSH', 'BRAIN_BATTLE') NOT NULL,
    difficulty ENUM('EASY', 'MEDIUM', 'HARD') NOT NULL,
    category_id INT NOT NULL,
    host_nickname VARCHAR(50) NOT NULL,
    total_players INT NOT NULL DEFAULT 1,
    winner_nickname VARCHAR(50) NULL,
    winner_score INT DEFAULT 0,
    started_at TIMESTAMP NULL,
    ended_at TIMESTAMP NULL,
    CONSTRAINT fk_matches_category FOREIGN KEY (category_id) REFERENCES categories(id),
    INDEX idx_room_code (room_code),
    INDEX idx_ended_at (ended_at)
) ENGINE=InnoDB;

-- 4. Match Players
CREATE TABLE IF NOT EXISTS match_players (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    match_id BIGINT NOT NULL,
    player_id VARCHAR(64) NOT NULL,
    nickname VARCHAR(50) NOT NULL,
    final_score INT NOT NULL DEFAULT 0,
    correct_answers INT NOT NULL DEFAULT 0,
    wrong_answers INT NOT NULL DEFAULT 0,
    avg_response_time_ms INT NOT NULL DEFAULT 0,
    rank_position INT NOT NULL DEFAULT 1,
    CONSTRAINT fk_match_players_match FOREIGN KEY (match_id) REFERENCES game_matches(id) ON DELETE CASCADE,
    INDEX idx_match_player (match_id, player_id)
) ENGINE=InnoDB;

-- 5. Match Answers
CREATE TABLE IF NOT EXISTS match_answers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    match_id BIGINT NOT NULL,
    player_id VARCHAR(64) NOT NULL,
    question_id INT NOT NULL,
    question_index INT NOT NULL,
    selected_option ENUM('A', 'B', 'C', 'D') NOT NULL,
    is_correct BOOLEAN NOT NULL DEFAULT FALSE,
    response_time_ms INT NOT NULL DEFAULT 0,
    points_awarded INT NOT NULL DEFAULT 0,
    answered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_match_answers_match FOREIGN KEY (match_id) REFERENCES game_matches(id) ON DELETE CASCADE,
    CONSTRAINT fk_match_answers_question FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE,
    INDEX idx_match_question (match_id, question_id)
) ENGINE=InnoDB;

-- SEED CATEGORIES
INSERT INTO categories (id, name, slug, description, icon_name) VALUES
(1, 'Programming & Languages', 'programming', 'Core OOP, Java, C++, Python, closures, and language idioms.', 'Code'),
(2, 'Data Structures & Algorithms', 'dsa', 'Arrays, Trees, Graphs, Dynamic Programming, and complexity analysis.', 'BinaryTree'),
(3, 'Database Management Systems', 'dbms', 'SQL, Indexing, ACID transactions, normalization, and locking protocols.', 'Database'),
(4, 'Operating Systems', 'os', 'Process scheduling, virtual memory, concurrency, deadlocks, and IPC.', 'Cpu'),
(5, 'Computer Networks', 'networks', 'TCP/IP stack, routing protocols, DNS, TLS/SSL, and sockets.', 'Network'),
(6, 'AI & Technology', 'ai-tech', 'Machine learning concepts, LLMs, neural networks, cloud, and distributed systems.', 'Sparkles')
ON DUPLICATE KEY UPDATE name=VALUES(name), description=VALUES(description);

-- SEED QUESTIONS (96 Quality Questions: 16 per category across EASY, MEDIUM, HARD)
INSERT INTO questions (category_id, difficulty, question_text, code_snippet, option_a, option_b, option_c, option_d, correct_option, explanation) VALUES
(1, 'EASY', 'What is the output of the following Java snippet regarding string equality?', 'String a = "devclash";
String b = new String("devclash");
System.out.println(a == b);', 'true', 'false', 'Compilation Error', 'NullPointerException', 'B', 'The ''=='' operator compares object references. String ''b'' is allocated on the heap via new, whereas ''a'' refers to a literal in the string intern pool.'),
(1, 'EASY', 'In Python, what is the data type of the expression: (42,)?', 'val = (42,)
print(type(val))', '<class ''int''>', '<class ''tuple''>', '<class ''list''>', '<class ''set''>', 'B', 'In Python, a trailing comma inside parentheses designates a single-element tuple. Without the comma, (42) evaluates to a plain integer.'),
(1, 'EASY', 'Which Java keyword prevents a class from being subclassed / inherited?', NULL, 'static', 'sealed', 'final', 'abstract', 'C', 'Applying the ''final'' modifier to a Java class declaration prohibits any other class from extending it.'),
(1, 'EASY', 'What is the size in bytes of a standard char pointer (char*) on a 64-bit architecture in C?', 'char *ptr = "DevClash";
printf("%zu", sizeof(ptr));', '1 byte', '4 bytes', '8 bytes', 'Depends on string length', 'C', 'On modern 64-bit platforms, memory addresses occupy 64 bits (8 bytes), regardless of the type being pointed to.'),
(1, 'EASY', 'In C++, which stream operator is used with std::cin to read formatted input?', NULL, '<< (Insertion)', '>> (Extraction)', '-> (Arrow)', ':: (Scope resolution)', 'B', 'The ''>>'' extraction operator extracts data from input streams such as std::cin into variables.'),
(1, 'MEDIUM', 'What does the following Python snippet print?', 'nums = [1, 2, 3, 4]
result = [x * 2 for x in nums if x % 2 == 0]
print(result)', '[2, 4, 6, 8]', '[4, 8]', '[2, 6]', '[4, 6, 8]', 'B', 'The list comprehension filters for even numbers (2 and 4), then multiplies each by 2, yielding [4, 8].'),
(1, 'MEDIUM', 'What runtime exception occurs when executing this Java code?', 'List<String> list = List.of("Alpha", "Beta");
list.add("Gamma");', 'IndexOutOfBoundsException', 'UnsupportedOperationException', 'NullPointerException', 'IllegalStateException', 'B', 'List.of() produces an immutable list in Java 9+. Any attempt to mutate it (add, remove, set) throws UnsupportedOperationException.'),
(1, 'MEDIUM', 'In C++, what is the primary purpose of declaring a base class destructor as ''virtual''?', 'class Base {
public:
    virtual ~Base() {}
};', 'To prevent heap allocation of Base instances', 'To ensure derived class destructors are called when deleting via a Base*', 'To force Base to become an abstract class', 'To auto-initialize all pointer members to nullptr', 'B', 'Without a virtual destructor, deleting a derived object through a Base pointer causes undefined behavior, often leaking derived resources.'),
(1, 'MEDIUM', 'What does the following C code output?', '#include <stdio.h>
int main() {
    int a = 5;
    int b = a++ + ++a;
    printf("%d", b);
    return 0;
}', '12', 'Undefined Behavior', '10', '11', 'B', 'Modifying a variable multiple times without an intervening sequence point invokes Undefined Behavior in C/C++ standard specifications.'),
(1, 'MEDIUM', 'Since Java 8, which memory region replaces PermGen and is allocated out of native OS memory?', NULL, 'Survivor Space', 'Metaspace', 'Eden Space', 'Old Generation', 'B', 'Java 8 replaced PermGen with Metaspace, allowing class metadata to grow dynamically within native host RAM.'),
(1, 'MEDIUM', 'What will be printed when running this Python script?', 'a = [1, 2, 3]
b = a
b += [4]
print(len(a))', '3', '4', 'TypeError', '7', 'B', 'For lists, ''+='' executes in-place mutation (calling __iadd__ / extend). Both ''a'' and ''b'' refer to the same list instance, which now has 4 elements.'),
(1, 'HARD', 'Under the Java Memory Model (JMM), what happens-before guarantee does a write to a ''volatile'' variable establish?', NULL, 'A write to volatile is atomic only for 32-bit types', 'A write to volatile happens-before every subsequent read of that volatile variable', 'A write to volatile locks the enclosing class object', 'Volatile writes are buffered locally and flushed once per second', 'B', 'The JMM specifies that a write to a volatile variable establishes a happens-before relationship with any subsequent read of that variable by any thread.'),
(1, 'HARD', 'Predict the output of the following Python function calls:', 'def append_item(x, target=[]):
    target.append(x)
    return target

print(append_item(1), append_item(2))', '[1] [2]', '[1] [1, 2]', '[1, 2] [1, 2]', 'TypeError: target is immutable', 'C', 'Default arguments are evaluated once when the function definition is executed. Both calls modify and return the exact same persistent list.'),
(1, 'HARD', 'In modern C++11/14/17, what does std::move(obj) actually do at runtime?', 'std::string a = "battle";
std::string b = std::move(a);', 'Physically copies the string buffers into register memory', 'Performs an unconditional static_cast to an rvalue reference (T&&)', 'Destroys ''a'' immediately before initializing ''b''', 'Locks the memory bus for atomic pointer reassignment', 'B', 'std::move does not move anything at runtime; it performs a static cast to an rvalue reference (T&&), enabling overload resolution to select the move constructor.'),
(1, 'HARD', 'What is the output of the following C program?', '#include <stdio.h>
int main() {
    int arr[] = {10, 20, 30, 40, 50};
    int *p = arr;
    printf("%d", *(p + 3) - *(p + 1));
    return 0;
}', '10', '20', '30', '2', 'B', '*(p + 3) yields arr[3] (40). *(p + 1) yields arr[1] (20). 40 - 20 = 20.'),
(1, 'HARD', 'Which modern OpenJDK garbage collector uses colored pointers and load barriers to achieve sub-millisecond maximum pause times?', NULL, 'Serial GC', 'Parallel GC', 'CMS (Concurrent Mark Sweep)', 'ZGC (Z Garbage Collector)', 'D', 'ZGC achieves sub-millisecond pause times regardless of heap size (from megabytes to terabytes) using reference coloring and load barriers.'),
(2, 'EASY', 'What is the worst-case time complexity of searching for an element in an unsorted array of size N?', NULL, 'O(1)', 'O(log N)', 'O(N)', 'O(N log N)', 'C', 'In an unsorted array, linear search must examine every element in the worst case, requiring O(N) operations.'),
(2, 'EASY', 'Which data structure operates strictly on a First-In-First-Out (FIFO) principle?', NULL, 'Stack', 'Queue', 'Priority Queue', 'Max Heap', 'B', 'Queues maintain FIFO ordering: items are inserted at the rear and removed from the front in order of arrival.'),
(2, 'EASY', 'What is the average time complexity of finding a key in a balanced Binary Search Tree (AVL or Red-Black Tree)?', NULL, 'O(1)', 'O(log N)', 'O(N)', 'O(N^2)', 'B', 'A self-balancing BST restricts tree height to O(log N), guaranteeing logarithmic search, insertion, and deletion.'),
(2, 'EASY', 'In a singly linked list with head pointer only, what is the time complexity of deleting the last element?', NULL, 'O(1)', 'O(log N)', 'O(N)', 'O(N^2)', 'C', 'To delete the last node, you must traverse to the second-to-last node to update its next pointer to null, requiring O(N) steps.'),
(2, 'EASY', 'Which sorting algorithm is known for having a best-case time complexity of O(N) on an already sorted array?', NULL, 'Merge Sort', 'Bubble Sort (with swapped flag) / Insertion Sort', 'Selection Sort', 'Quick Sort (naive)', 'B', 'Insertion Sort and optimized Bubble Sort detect when no swaps are performed during the first pass, terminating in O(N) time.'),
(2, 'MEDIUM', 'Given a Binary Search Tree with nodes inserted in order: [10, 5, 15, 3, 7], what is its In-Order traversal?', NULL, '10, 5, 3, 7, 15', '3, 5, 7, 10, 15', '3, 7, 5, 15, 10', '15, 10, 7, 5, 3', 'B', 'In-Order traversal (Left -> Root -> Right) of any valid Binary Search Tree always outputs keys in strictly ascending sorted order.'),
(2, 'MEDIUM', 'Which algorithm finds the single-source shortest path in a weighted graph with non-negative edge weights in O((V + E) log V) time?', NULL, 'Breadth-First Search (BFS)', 'Dijkstra''s Algorithm with Min-Heap', 'Floyd-Warshall Algorithm', 'Kruskal''s Algorithm', 'B', 'Dijkstra''s algorithm using an adjacency list and min-priority queue achieves O((V + E) log V) for non-negative edge weights.'),
(2, 'MEDIUM', 'Under what condition does QuickSort degrade to its worst-case time complexity of O(N^2)?', NULL, 'When elements are distinct and randomized', 'When partitions are maximally unbalanced (e.g. sorted array with first element as pivot)', 'When using Median-of-Three pivot selection', 'When array size is an exact power of 2', 'B', 'If partitions produce subproblems of size 0 and N-1 at every recursive level, call stack depth becomes N, resulting in O(N^2) comparisons.'),
(2, 'MEDIUM', 'What is the minimum number of standard single-ended FIFO queues required to implement a LIFO Stack?', NULL, '1', '2', '3', 'Impossible with queues', 'B', 'A stack can be simulated using 2 queues: either by making push costly (transferring elements) or by making pop costly.'),
(2, 'MEDIUM', 'In a directed graph, which algorithm can detect cycles and produce a valid topological ordering?', NULL, 'Prim''s Algorithm', 'Kahn''s Algorithm (in-degree BFS) or DFS with recursion stack tracking', 'Binary Search', 'Kadane''s Algorithm', 'B', 'Kahn''s algorithm uses in-degree tracking via BFS. If the final ordered list contains fewer than V nodes, a cycle exists.'),
(2, 'MEDIUM', 'What is the primary drawback of Linear Probing for open-address hash collision resolution?', NULL, 'Requires double the pointer memory of separate chaining', 'Primary Clustering (long contiguous blocks of occupied buckets)', 'Cannot handle string keys', 'Causes infinite recursion during table resize', 'B', 'Linear probing creates runs of occupied slots (primary clustering), which degrades average lookup and insertion performance to O(N).'),
(2, 'HARD', 'In Kadane''s algorithm for finding the maximum contiguous subarray sum, what are the time and auxiliary space complexities?', 'int maxSubArray(int[] nums) {
    int cur = nums[0], max = nums[0];
    for (int i = 1; i < nums.length; i++) {
        cur = Math.max(nums[i], cur + nums[i]);
        max = Math.max(max, cur);
    }
    return max;
}', 'Time: O(N log N), Space: O(N)', 'Time: O(N), Space: O(1)', 'Time: O(N^2), Space: O(1)', 'Time: O(N), Space: O(N)', 'B', 'Kadane inspects each array element exactly once in a single linear scan while keeping track of current and maximum sum in O(1) space.'),
(2, 'HARD', 'What is the amortized time complexity per operation in a Disjoint Set Union (Union-Find) structure with both union-by-rank and path compression?', NULL, 'O(log N)', 'O(alpha(N)) where alpha is the Inverse Ackermann function', 'O(N)', 'O(sqrt(N))', 'B', 'With both union-by-rank and path compression heuristics, any sequence of M operations on N elements runs in O(M * alpha(N)) time, which is practically constant.'),
(2, 'HARD', 'Which algorithm computes all Strongly Connected Components (SCCs) of a directed graph in a single DFS pass in O(V + E) time?', NULL, 'Tarjan''s SCC Algorithm', 'Bellman-Ford Algorithm', 'Boruvka''s Algorithm', 'Floyd-Warshall Algorithm', 'A', 'Tarjan''s algorithm uses discovery times and low-link values in a single DFS traversal with a stack to identify SCCs in linear O(V + E) time.'),
(2, 'HARD', 'In a 0-indexed array representation of a complete binary heap, what is the index of the parent of a node at index ''i'' (where i > 0)?', NULL, '(i - 1) / 2', 'i / 2', '(i + 1) / 2', '2 * i + 1', 'A', 'In 0-based array heaps, node ''i'' has left child at 2i + 1, right child at 2i + 2, and parent at floor((i - 1) / 2).'),
(2, 'HARD', 'What is the time complexity to search for a word of length L in a Trie containing N words of average length L?', NULL, 'O(N * L)', 'O(L)', 'O(log N)', 'O(L * log N)', 'B', 'Searching in a Trie depends solely on the length of the query key (L), independent of the total number of words (N) stored.'),
(3, 'EASY', 'Which SQL clause is used to filter records resulting from an aggregate GROUP BY operation?', 'SELECT dept, COUNT(*) FROM emp GROUP BY dept ___ COUNT(*) > 5;', 'WHERE', 'HAVING', 'FILTER', 'LIMIT', 'B', 'HAVING filters aggregated groups after the GROUP BY phase, whereas WHERE filters individual rows before grouping.'),
(3, 'EASY', 'What does the ''A'' in ACID database transaction properties represent?', NULL, 'Availability', 'Atomicity', 'Authentication', 'Asynchronous', 'B', 'ACID stands for Atomicity (all-or-nothing), Consistency, Isolation, and Durability.'),
(3, 'EASY', 'Which Normal Form (NF) mandates that all non-key attributes are fully functionally dependent on the entire primary key (no partial dependencies)?', NULL, 'First Normal Form (1NF)', 'Second Normal Form (2NF)', 'Third Normal Form (3NF)', 'Boyce-Codd Normal Form (BCNF)', 'B', '2NF requires 1NF and guarantees no partial dependencies: every non-prime attribute must depend on the whole candidate key.'),
(3, 'EASY', 'Which SQL statement is classified as Data Definition Language (DDL)?', NULL, 'UPDATE', 'ALTER TABLE', 'INSERT INTO', 'DELETE FROM', 'B', 'ALTER TABLE defines or modifies database schema structure and is classified as DDL. INSERT, UPDATE, and DELETE are DML.'),
(3, 'EASY', 'What is the primary role of a Foreign Key constraint in relational databases?', NULL, 'To encrypt user passwords automatically', 'To enforce referential integrity between two related tables', 'To speed up Full-Text search queries', 'To force all text values into UPPERCASE', 'B', 'A Foreign Key ensures referential integrity by enforcing that a column''s value matches a primary key in another table.'),
(3, 'MEDIUM', 'Why do relational database engines (like MySQL InnoDB) prefer B+ Trees over Binary Search Trees for disk-based indexing?', NULL, 'B+ Trees hold fewer total keys per gigabyte', 'High fan-out yields low tree height, minimizing disk I/O operations', 'Binary trees cannot be saved to SSD storage', 'B+ Trees prevent all SQL injection attacks', 'B', 'B+ Trees have hundreds or thousands of keys per page (high fan-out), keeping depth at 3-4 levels and requiring only a few disk block reads.'),
(3, 'MEDIUM', 'Which SQL transaction isolation level prevents Dirty Reads, Non-Repeatable Reads, and Phantom Reads?', NULL, 'Read Uncommitted', 'Read Committed', 'Repeatable Read', 'Serializable', 'D', 'Serializable is the highest isolation level, completely isolating concurrent transactions against all three read phenomena.'),
(3, 'MEDIUM', 'What is the primary distinction between TRUNCATE and DELETE in SQL?', NULL, 'DELETE drops table schema, TRUNCATE does not', 'TRUNCATE is a DDL operation deallocating data pages, while DELETE is DML logging row deletions', 'TRUNCATE triggers row-level AFTER DELETE triggers', 'DELETE cannot be rolled back in transactions', 'B', 'TRUNCATE is DDL that resets table pages and high-water mark without firing row-level triggers, making it much faster than row-by-row DELETE.'),
(3, 'MEDIUM', 'What type of JOIN returns all records from Table A, along with matching rows from Table B, filling missing rows with NULL?', 'SELECT * FROM TableA ___ JOIN TableB ON TableA.id = TableB.a_id;', 'INNER JOIN', 'LEFT OUTER JOIN', 'CROSS JOIN', 'NATURAL INNER JOIN', 'B', 'A LEFT OUTER JOIN preserves all rows from the left table regardless of whether a matching record exists in the right table.'),
(3, 'MEDIUM', 'In Strict Two-Phase Locking (Strict 2PL), when are exclusive locks (X-locks) held by a transaction released?', NULL, 'Immediately after each SELECT query finishes', 'Only at the end of the transaction (COMMIT or ROLLBACK)', 'As soon as the shrinking phase begins', 'Whenever a foreign key check succeeds', 'B', 'Strict 2PL holds all exclusive locks until transaction completion (commit/abort), preventing cascading aborts.'),
(3, 'MEDIUM', 'Given a composite index on (dept_id, salary), which query can fully utilize the index for filtering?', 'CREATE INDEX idx_dept_sal ON employees(dept_id, salary);', 'WHERE salary > 50000', 'WHERE dept_id = 10 AND salary > 50000', 'WHERE salary = 70000', 'WHERE YEAR(hire_date) = 2024', 'B', 'Composite B-Tree indexes follow the leftmost-prefix rule: queries must filter on the leading column (dept_id) to leverage subsequent columns.'),
(3, 'HARD', 'In MySQL InnoDB storage engine, what is stored in the leaf nodes of a secondary (non-clustered) index?', NULL, 'Direct disk block offsets to physical cylinders', 'The primary key value of the row', 'The full complete row data duplicated', 'The transaction undo log pointer', 'B', 'InnoDB secondary indexes store the indexed columns plus the corresponding Primary Key value, which is then used to traverse the clustered index.'),
(3, 'HARD', 'According to Brewer''s CAP Theorem, when a network partition (P) occurs in a distributed database, what trade-off must be made?', NULL, 'Atomicity vs Durability', 'Consistency vs Availability', 'Encryption vs Compression', 'Latency vs Replication Factor', 'B', 'When network partitions (P) happen, a distributed system must choose between returning consistent latest data (C) or responding to all requests (A).'),
(3, 'HARD', 'How does MySQL InnoDB implement Multi-Version Concurrency Control (MVCC) for non-locking consistent reads?', NULL, 'By locking all tables in exclusive mode during reads', 'By combining Read Views with Undo Log record chains', 'By copying the entire database to RAM for each connection', 'By aborting any write that clashes with a read timestamp', 'B', 'InnoDB builds consistent row snapshots on the fly by traversing roll_ptr links in Undo Log segments according to the transaction''s Read View.'),
(3, 'HARD', 'What fundamental rule does the Write-Ahead Logging (WAL) protocol enforce before dirty pages are flushed to table data files?', NULL, 'All table constraints must be dropped temporarily', 'Corresponding log records describing the update must be flushed to stable disk storage first', 'The database must wait for 60 seconds of zero network activity', 'Secondary indexes must be dropped and recreated', 'B', 'WAL dictates that redo/undo log records must reach non-volatile disk before the corresponding modified data page can be written, guaranteeing Durability.'),
(3, 'HARD', 'What condition must hold for every non-trivial functional dependency X -> Y in Boyce-Codd Normal Form (BCNF)?', NULL, 'Y must be a prime attribute', 'X must be a superkey', 'Y must be a foreign key', 'X and Y must be numeric columns', 'B', 'BCNF requires that for every functional dependency X -> Y, the determinant X must be a superkey of the relation.'),
(4, 'EASY', 'Which process state occurs when an executing process issues an I/O request and awaits hardware completion?', NULL, 'Ready', 'Blocked / Waiting', 'Terminated', 'Zombie', 'B', 'A process transitions to the Blocked/Waiting state until the asynchronous I/O completion interrupt is serviced by the OS.'),
(4, 'EASY', 'What is the primary responsibility of the OS CPU scheduler Dispatcher?', NULL, 'Translating C code to binary machine instructions', 'Performing context switching and giving CPU control to the selected process', 'Allocating physical disk sectors to swap partitions', 'Assigning IP addresses to Ethernet interfaces', 'B', 'The dispatcher performs context switching, toggles the CPU to user mode, and jumps to the program counter of the chosen thread.'),
(4, 'EASY', 'Which of the following is NOT one of the four necessary Coffman conditions for Deadlock to occur?', NULL, 'Mutual Exclusion', 'Hold and Wait', 'Preemption Allowed', 'Circular Wait', 'C', 'The condition is NO PREEMPTION. If resources can be preempted forcibly, deadlock cannot exist.'),
(4, 'EASY', 'In Linux, which system call creates an exact duplicate child process sharing the parent''s execution state?', NULL, 'exec()', 'fork()', 'pthread_create()', 'signal()', 'B', 'fork() creates a new child process with its own PID by duplicating the address space of the caller using copy-on-write.'),
(4, 'EASY', 'What is the smallest unit of execution that an operating system kernel schedules on a CPU core?', NULL, 'Process', 'Thread', 'Interrupt Handler', 'Virtual Page', 'B', 'A thread (lightweight process) is the smallest schedulable execution entity that shares memory space with peer threads in a process.'),
(4, 'MEDIUM', 'What condition occurs when the OS spends more time swapping virtual memory pages than executing CPU instructions?', NULL, 'Deadlock', 'Thrashing', 'Starvation', 'External Fragmentation', 'B', 'Thrashing happens when the aggregate working sets of all active processes exceed physical RAM, causing constant page faults.'),
(4, 'MEDIUM', 'What is a ''Zombie Process'' in Unix-like operating systems?', NULL, 'A background daemon running forever', 'A terminated process whose parent has not yet collected its exit code via wait()', 'A process whose parent terminated while it was still executing', 'A process that consumed 100% of available CPU', 'B', 'A zombie process has exited, but retains an entry in the kernel process table until its parent reads its exit status with wait().'),
(4, 'MEDIUM', 'Which CPU scheduling algorithm frequently suffers from the ''Convoy Effect''?', NULL, 'Round Robin (RR)', 'First-Come, First-Served (FCFS)', 'Shortest Job First (Preemptive)', 'Multilevel Feedback Queue', 'B', 'In FCFS, when a long CPU-burst process monopolizes the CPU, short I/O-bound processes queue behind it, reducing CPU and device utilization.'),
(4, 'MEDIUM', 'Which IPC mechanism provides the fastest data exchange between processes running on the same physical host?', NULL, 'Named Pipes (FIFOs)', 'Shared Memory (shmget / mmap)', 'UNIX Domain Sockets', 'Message Queues', 'B', 'Shared memory maps the same physical RAM frames into multiple process address spaces, avoiding redundant kernel-space copying.'),
(4, 'MEDIUM', 'Three processes arrive at time 0 with burst times P1=24, P2=3, P3=3. Under FCFS (order P1, P2, P3), what is the average waiting time?', 'P1 wait = 0
P2 wait = 24
P3 wait = 24 + 3 = 27', '17 ms', '27 ms', '15 ms', '10 ms', 'A', 'Waiting times: P1=0, P2=24, P3=27. Average = (0 + 24 + 27) / 3 = 51 / 3 = 17 ms.'),
(4, 'MEDIUM', 'A counting semaphore S is initialized to 7. If 12 wait() (P) operations and 8 signal() (V) operations are performed, what is the final value of S?', 'Initial = 7
12 * wait (decrement)
8 * signal (increment)', '1', '3', '0', '-1', 'B', 'Formula: Final = Initial - waitCount + signalCount = 7 - 12 + 8 = 3.'),
(4, 'HARD', 'What is the primary function of the hardware Translation Lookaside Buffer (TLB)?', NULL, 'To queue network TCP packets before sending', 'To cache virtual-to-physical page frame translations, avoiding multi-level RAM page table lookups', 'To buffer writes to SSD flash blocks', 'To store active process register states during sleep', 'B', 'The TLB is an associative hardware cache that holds recent virtual-to-physical address mappings, speeding up memory accesses significantly.'),
(4, 'HARD', 'Which page replacement algorithm can exhibit Belady''s Anomaly (where increasing physical page frames can increase page faults)?', NULL, 'LRU (Least Recently Used)', 'FIFO (First-In, First-Out)', 'Optimal Page Replacement (OPT)', 'LFU (Least Frequently Used with aging)', 'B', 'FIFO is not a stack algorithm; certain reference strings generate more page faults when allocated more physical frames.'),
(4, 'HARD', 'In Peterson''s Algorithm for two-process mutual exclusion, which two shared state variables are required?', NULL, 'A counting semaphore and mutex pointer', 'A boolean array flag[2] and an integer turn variable', 'Two condition variables and a file lock', 'An atomic hardware compare-and-swap register', 'B', 'Peterson''s algorithm relies on flag[i] to signify interest in entering the critical section, and ''turn'' to resolve simultaneous competition.'),
(4, 'HARD', 'What fundamental data structures are maintained by Dijkstra''s Banker''s Algorithm for deadlock avoidance?', NULL, 'Adjacency List and Priority Queue', 'Available vector, Max matrix, Allocation matrix, and Need matrix', 'Hash Table and Red-Black Tree', 'Ring buffer and Circular Linked List', 'B', 'The Banker''s Algorithm tracks Available[m], Max[n][m], Allocation[n][m], and Need[n][m] (where Need = Max - Allocation) to ensure safe states.'),
(4, 'HARD', 'What is the primary advantage of an Inverted Page Table over conventional multi-level hierarchical page tables?', NULL, 'Eliminates the need for any hardware TLB', 'Memory overhead scales with physical RAM size rather than the aggregate virtual address spaces', 'Guarantees zero page faults at runtime', 'Translates addresses without CPU assistance', 'B', 'An inverted page table has one entry per physical frame instead of per virtual page, drastically reducing page table memory footprint on 64-bit systems.'),
(5, 'EASY', 'What is the default standard port number used for secure HTTPS web traffic?', NULL, '80', '443', '8080', '22', 'B', 'HTTPS runs over port 443 by default, whereas unencrypted HTTP operates over port 80.'),
(5, 'EASY', 'Which application layer protocol resolves human-friendly domain names (e.g. google.com) into IP addresses?', NULL, 'DHCP', 'ARP', 'DNS', 'ICMP', 'C', 'DNS (Domain Name System) translates textual hostnames to numerical IPv4/IPv6 addresses.'),
(5, 'EASY', 'Which layer of the OSI model provides end-to-end reliable data delivery, flow control, and error detection?', NULL, 'Network Layer', 'Transport Layer', 'Data Link Layer', 'Physical Layer', 'B', 'The Transport Layer (Layer 4, e.g., TCP) ensures reliable delivery, segment reordering, congestion control, and flow control.'),
(5, 'EASY', 'What is the standard size of an IPv4 address in bits and bytes?', NULL, '16 bits (2 bytes)', '32 bits (4 bytes)', '64 bits (8 bytes)', '128 bits (16 bytes)', 'B', 'An IPv4 address consists of 32 bits, typically represented as 4 dotted-decimal octets (e.g. 192.168.1.1).'),
(5, 'EASY', 'Which network utility uses ICMP Echo Request and Echo Reply messages to test host reachability?', NULL, 'traceroute', 'ping', 'netstat', 'nslookup', 'B', '''ping'' sends ICMP Type 8 (Echo Request) packets and measures the round-trip latency when receiving ICMP Type 0 (Echo Reply).'),
(5, 'MEDIUM', 'What is the exact sequence of control flags exchanged during the TCP Three-Way Handshake?', NULL, 'ACK -> SYN -> SYN-ACK', 'SYN -> SYN-ACK -> ACK', 'FIN -> ACK -> FIN-ACK', 'SYN -> PSH -> ACK', 'B', 'The client sends a SYN packet; the server replies with SYN-ACK; the client acknowledges with ACK to establish the connection.'),
(5, 'MEDIUM', 'What is the primary role of the Address Resolution Protocol (ARP) in local area networks?', NULL, 'Mapping a domain name to an IP address', 'Mapping a known IPv4 address to a physical MAC address on the local link', 'Encrypting payload packets for VPN tunnels', 'Allocating subnet masks dynamically', 'B', 'ARP broadcasts on the local data link layer to find the physical Ethernet MAC address corresponding to a target IPv4 address.'),
(5, 'MEDIUM', 'Why does the initiating endpoint in a TCP connection termination enter the TIME_WAIT state (2*MSL)?', NULL, 'To renegotiate SSL/TLS encryption certificates', 'To ensure delayed duplicate packets drain from the network and retransmit the final ACK if lost', 'To compress socket buffers into gzip format', 'To force the operating system to reboot its network interface', 'B', 'TIME_WAIT prevents duplicate segments from an old connection from corrupting a new connection and guarantees the peer received the final ACK.'),
(5, 'MEDIUM', 'How many usable host IP addresses are available in a standard IPv4 /27 subnet?', 'Subnet mask: 255.255.255.224 (/27)', '32', '30', '62', '14', 'B', 'A /27 subnet has 32 - 27 = 5 host bits. Total addresses = 2^5 = 32. Subtracting 2 (network address and broadcast address) gives 30 usable host IPs.'),
(5, 'MEDIUM', 'What is the four-step message exchange sequence (DORA) used by DHCP to assign network configurations?', NULL, 'Discover -> Offer -> Request -> Acknowledge', 'Deliver -> Open -> Receive -> Accept', 'Demand -> Order -> Route -> Assign', 'Detect -> Organize -> Replicate -> Approve', 'A', 'DHCP follows DORA: Client sends Discover, Server replies with Offer, Client sends Request, Server responds with Acknowledge.'),
(5, 'MEDIUM', 'Which routing protocol uses the Dijkstra algorithm to calculate the shortest path tree within an Autonomous System (AS)?', NULL, 'RIP (Routing Information Protocol)', 'OSPF (Open Shortest Path First)', 'BGP (Border Gateway Protocol)', 'EGP (Exterior Gateway Protocol)', 'B', 'OSPF is a link-state routing protocol that distributes link-state advertisements and uses Dijkstra''s algorithm to compute shortest paths.'),
(5, 'HARD', 'In TCP congestion control, how does the congestion window (cwnd) grow during the Slow Start phase?', NULL, 'Increases linearly by 1 MSS per RTT', 'Doubles exponentially every Round-Trip Time (RTT)', 'Triples on every incoming packet', 'Remains static until a timeout occurs', 'B', 'In Slow Start, cwnd increments by 1 MSS for every received ACK, effectively doubling the congestion window every round-trip time.'),
(5, 'HARD', 'How does HTTP/2 eliminate the application-layer Head-of-Line (HoL) blocking found in HTTP/1.1?', NULL, 'By using UDP instead of TCP', 'By framing messages into binary chunks multiplexed concurrently over a single TCP stream', 'By removing headers completely from requests', 'By opening a separate TCP connection for every image asset', 'B', 'HTTP/2 introduces a binary framing layer that breaks requests/responses into discrete interleaved frames sent over a single TCP connection.'),
(5, 'HARD', 'What transport-layer protocol does HTTP/3 operate over to overcome TCP-level Head-of-Line blocking across packet loss?', NULL, 'SCTP', 'QUIC (built on UDP)', 'RSVP', 'IPsec', 'B', 'HTTP/3 uses QUIC running over UDP, where lost packets on one logical stream do not stall independent concurrent streams.'),
(5, 'HARD', 'Why is BGP (Border Gateway Protocol) classified as a Path-Vector protocol rather than a pure Distance-Vector protocol?', NULL, 'It uses Euclidean vector distances in kilometers', 'It advertises the complete sequence of Autonomous System Numbers (AS_PATH) to prevent routing loops', 'It only routes UDP multicast packets', 'It requires all routers on Earth to share a single master password', 'B', 'BGP includes the AS_PATH attribute in routing advertisements, allowing routers to detect and drop updates that already contain their own AS number.'),
(5, 'HARD', 'In WebRTC and real-time distributed networking, what do STUN and TURN protocols accomplish?', NULL, 'Compile JavaScript to WebAssembly', 'Assist clients behind NAT/firewalls in discovering public IP/ports (STUN) or relaying media when P2P fails (TURN)', 'Balance MySQL database transactions across read replicas', 'Provide 4-way WPA3 WiFi handshakes', 'B', 'STUN allows NATed clients to find their external IP:port mapping, while TURN acts as a fallback relay server when symmetric NAT blocks direct peer-to-peer connection.'),
(6, 'EASY', 'What does the acronym ''LLM'' stand for in modern Artificial Intelligence?', NULL, 'Logical Learning Mechanism', 'Large Language Model', 'Linear Loss Matrix', 'Low-Latency Module', 'B', 'LLM stands for Large Language Model, referring to deep neural networks trained on vast text corpora (e.g. GPT, Gemini, Claude).'),
(6, 'EASY', 'Which activation function computes f(x) = max(0, x) and is widely used in deep neural network hidden layers?', NULL, 'Sigmoid', 'ReLU (Rectified Linear Unit)', 'Tanh', 'Softmax', 'B', 'ReLU outputs the input directly if positive, otherwise zero, mitigating the vanishing gradient problem in deep networks.'),
(6, 'EASY', 'What is the primary role of an API Gateway in a microservices and distributed computing architecture?', NULL, 'Direct disk storage for raw audio files', 'Single entry point routing requests, handling security, authentication, and rate limiting', 'Generating synthetic training data for GANs', 'Compiling bytecode into machine code', 'B', 'An API Gateway provides a centralized ingress point for client requests, handling routing, SSL termination, and rate limiting.'),
(6, 'EASY', 'Which type of machine learning involves an agent learning to make decisions by taking actions in an environment to maximize cumulative reward?', NULL, 'Supervised Learning', 'Reinforcement Learning', 'Unsupervised Clustering', 'Self-Supervised Masking', 'B', 'Reinforcement Learning (RL) trains agents using reward and penalty feedback in an environment to discover optimal policies.'),
(6, 'EASY', 'What is the primary security benefit of using HTTPS instead of plain HTTP?', NULL, 'Makes web pages load without CSS', 'Encrypts communication between client and server using TLS, preventing eavesdropping and tampering', 'Guarantees 100% server uptime', 'Prevents client browsers from opening developer tools', 'B', 'HTTPS encrypts the data transport using TLS, ensuring confidentiality, data integrity, and server authentication.'),
(6, 'MEDIUM', 'What core mechanism introduced in ''Attention Is All You Need'' (Vaswani et al., 2017) allows Transformers to process sequences in parallel?', NULL, 'Recurrent Hidden State Passing', 'Multi-Head Self-Attention', 'Convolutional Max-Pooling', 'Markov Decision Processes', 'B', 'Self-attention computes relationships between all tokens in a sequence simultaneously using Query, Key, and Value matrices without recurrent loops.'),
(6, 'MEDIUM', 'In distributed consensus protocols like Raft, what are the three possible states of any participating node?', NULL, 'Master, Worker, Arbiter', 'Leader, Follower, Candidate', 'Proposer, Acceptor, Learner', 'Active, Standby, Terminated', 'B', 'In Raft, a node is in one of three states: Follower (responds to RPCs), Candidate (runs for election), or Leader (handles client requests).'),
(6, 'MEDIUM', 'What is the primary operational flow of a Retrieval-Augmented Generation (RAG) system?', NULL, 'Fine-tuning base neural network weights on every user query', 'Querying a vector database for relevant context embeddings and augmenting the prompt before LLM generation', 'Compressing text into zip files before passing to model', 'Executing SQL commands directly inside the GPU register file', 'B', 'RAG converts user queries to embeddings, retrieves relevant chunks from a vector index, and feeds them as grounded context to the LLM.'),
(6, 'MEDIUM', 'What security mechanism effectively prevents Cross-Site Request Forgery (CSRF) attacks in modern web apps?', NULL, 'Disabling TLS/SSL certificates', 'Anti-CSRF tokens (Synchronizer Token Pattern) and SameSite cookie attributes', 'Minifying JavaScript bundle files', 'Using Base64 encoding on query parameters', 'B', 'Anti-CSRF tokens ensure requests originate from the legitimate site, while SameSite=Strict/Lax cookies prevent browsers from sending cookies on cross-origin requests.'),
(6, 'MEDIUM', 'Which two Linux kernel features provide process isolation and hardware resource limiting respectively for Docker containers?', NULL, 'systemd and cron', 'Namespaces (isolation) and cgroups (resource limits)', 'iptables and swap', 'glibc and SELinux', 'B', 'Namespaces isolate process trees, network, and mount points; Control Groups (cgroups) meter and throttle CPU, memory, and disk I/O.'),
(6, 'MEDIUM', 'When a machine learning model achieves 99.8% training accuracy but only 52.1% validation accuracy, what is happening and what is a remedy?', NULL, 'Underfitting; add more layers and parameters', 'Overfitting; apply Dropout, L2 regularization, or gather more diverse data', 'Gradient explosion; double the learning rate', 'Quantization loss; convert FP16 to INT4', 'B', 'High training accuracy with poor validation accuracy is the textbook symptom of overfitting (memorizing noise), remedied by regularization and data augmentation.'),
(6, 'HARD', 'Why is Hierarchical Navigable Small World (HNSW) graphs widely preferred for production vector databases over brute-force kNN?', NULL, 'HNSW converts vectors into SQL relational tables', 'It enables Approximate Nearest Neighbor (ANN) search across millions of dense embeddings in O(log N) time', 'It forces all embeddings to have unit length 1.0', 'It removes the need for GPU accelerators during training', 'B', 'Brute-force search is O(N * D) which is intractable at scale. HNSW creates multi-layer skip-list graphs allowing sub-linear logarithmic ANN search.'),
(6, 'HARD', 'In Java RMI distributed architecture, what is the precise role of the client-side ''Stub'' object?', NULL, 'It compiles the Java bytecode directly inside client memory', 'It acts as a local proxy that marshals parameters, initiates socket communication with the remote skeleton, and unmarshals return values', 'It connects directly to MySQL via JDBC', 'It provides WebSocket handshake negotiation for React browsers', 'B', 'The RMI stub implements the remote interface on the client, transparently serializing parameters over network sockets to the remote server and returning results.'),
(6, 'HARD', 'According to Amdahl''s Law, if 20% of a program is strictly serial (P = 0.8), what is the maximum theoretical speedup limit regardless of how many parallel processor cores are added?', 'Speedup = 1 / ((1 - P) + P / N)
Serial fraction (1 - P) = 0.2', '2x', '5x', '10x', 'Infinite speedup', 'B', 'As N -> infinity, P / N -> 0. Maximum speedup = 1 / (1 - P) = 1 / 0.2 = 5x.'),
(6, 'HARD', 'In AI security, what is an ''Indirect Prompt Injection'' attack against an LLM-powered application?', NULL, 'Overclocking the GPU until weights overflow', 'Adversarial instructions embedded in untrusted external data (e.g. web pages, emails) that hijack the model''s control flow when processed', 'Guessing the API key through brute force dictionary attack', 'Extracting model weights by measuring electromagnetic radiation', 'B', 'Indirect prompt injection occurs when an LLM reads external untrusted content (like a webpage or document) containing malicious text that overrides system instructions.'),
(6, 'HARD', 'What fundamental property defines a Zero-Knowledge Proof (ZKP) in modern cryptography?', NULL, 'The proof is completely unencrypted and public', 'A prover can prove to a verifier that a statement is true without revealing any secret information beyond the validity of the statement itself', 'The algorithm requires zero CPU clock cycles to verify', 'The key must be shared across all nodes simultaneously', 'B', 'A Zero-Knowledge Proof guarantees Completeness, Soundness, and Zero-Knowledge: proving truth without exposing the underlying secret witness.');
