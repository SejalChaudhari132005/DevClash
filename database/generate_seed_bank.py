# -*- coding: utf-8 -*-
"""
Script to generate database/seed_questions.sql containing 96 high-quality questions
covering all 6 categories across EASY, MEDIUM, HARD, with code snippets, MCQ,
output prediction, find-the-bug, and logical reasoning.
"""
import json
import os

questions = [
    # =========================================================================
    # CATEGORY 1: Programming (Java, Python, C, C++) - 16 Questions
    # =========================================================================
    # 1. Easy - Java
    {
        "category_id": 1,
        "difficulty": "EASY",
        "question_text": "What is the output of the following Java snippet regarding string equality?",
        "code_snippet": "String a = \"devclash\";\nString b = new String(\"devclash\");\nSystem.out.println(a == b);",
        "option_a": "true",
        "option_b": "false",
        "option_c": "Compilation Error",
        "option_d": "NullPointerException",
        "correct_option": "B",
        "explanation": "The '==' operator compares object references. String 'b' is allocated on the heap via new, whereas 'a' refers to a literal in the string intern pool."
    },
    # 2. Easy - Python
    {
        "category_id": 1,
        "difficulty": "EASY",
        "question_text": "In Python, what is the data type of the expression: (42,)?",
        "code_snippet": "val = (42,)\nprint(type(val))",
        "option_a": "<class 'int'>",
        "option_b": "<class 'tuple'>",
        "option_c": "<class 'list'>",
        "option_d": "<class 'set'>",
        "correct_option": "B",
        "explanation": "In Python, a trailing comma inside parentheses designates a single-element tuple. Without the comma, (42) evaluates to a plain integer."
    },
    # 3. Easy - Java
    {
        "category_id": 1,
        "difficulty": "EASY",
        "question_text": "Which Java keyword prevents a class from being subclassed / inherited?",
        "code_snippet": None,
        "option_a": "static",
        "option_b": "sealed",
        "option_c": "final",
        "option_d": "abstract",
        "correct_option": "C",
        "explanation": "Applying the 'final' modifier to a Java class declaration prohibits any other class from extending it."
    },
    # 4. Easy - C
    {
        "category_id": 1,
        "difficulty": "EASY",
        "question_text": "What is the size in bytes of a standard char pointer (char*) on a 64-bit architecture in C?",
        "code_snippet": "char *ptr = \"DevClash\";\nprintf(\"%zu\", sizeof(ptr));",
        "option_a": "1 byte",
        "option_b": "4 bytes",
        "option_c": "8 bytes",
        "option_d": "Depends on string length",
        "correct_option": "C",
        "explanation": "On modern 64-bit platforms, memory addresses occupy 64 bits (8 bytes), regardless of the type being pointed to."
    },
    # 5. Easy - C++
    {
        "category_id": 1,
        "difficulty": "EASY",
        "question_text": "In C++, which stream operator is used with std::cin to read formatted input?",
        "code_snippet": None,
        "option_a": "<< (Insertion)",
        "option_b": ">> (Extraction)",
        "option_c": "-> (Arrow)",
        "option_d": ":: (Scope resolution)",
        "correct_option": "B",
        "explanation": "The '>>' extraction operator extracts data from input streams such as std::cin into variables."
    },
    # 6. Medium - Python (Predict Output)
    {
        "category_id": 1,
        "difficulty": "MEDIUM",
        "question_text": "What does the following Python snippet print?",
        "code_snippet": "nums = [1, 2, 3, 4]\nresult = [x * 2 for x in nums if x % 2 == 0]\nprint(result)",
        "option_a": "[2, 4, 6, 8]",
        "option_b": "[4, 8]",
        "option_c": "[2, 6]",
        "option_d": "[4, 6, 8]",
        "correct_option": "B",
        "explanation": "The list comprehension filters for even numbers (2 and 4), then multiplies each by 2, yielding [4, 8]."
    },
    # 7. Medium - Java (Find Bug)
    {
        "category_id": 1,
        "difficulty": "MEDIUM",
        "question_text": "What runtime exception occurs when executing this Java code?",
        "code_snippet": "List<String> list = List.of(\"Alpha\", \"Beta\");\nlist.add(\"Gamma\");",
        "option_a": "IndexOutOfBoundsException",
        "option_b": "UnsupportedOperationException",
        "option_c": "NullPointerException",
        "option_d": "IllegalStateException",
        "correct_option": "B",
        "explanation": "List.of() produces an immutable list in Java 9+. Any attempt to mutate it (add, remove, set) throws UnsupportedOperationException."
    },
    # 8. Medium - C++ (Destructor Concept)
    {
        "category_id": 1,
        "difficulty": "MEDIUM",
        "question_text": "In C++, what is the primary purpose of declaring a base class destructor as 'virtual'?",
        "code_snippet": "class Base {\npublic:\n    virtual ~Base() {}\n};",
        "option_a": "To prevent heap allocation of Base instances",
        "option_b": "To ensure derived class destructors are called when deleting via a Base*",
        "option_c": "To force Base to become an abstract class",
        "option_d": "To auto-initialize all pointer members to nullptr",
        "correct_option": "B",
        "explanation": "Without a virtual destructor, deleting a derived object through a Base pointer causes undefined behavior, often leaking derived resources."
    },
    # 9. Medium - C (Predict Output)
    {
        "category_id": 1,
        "difficulty": "MEDIUM",
        "question_text": "What does the following C code output?",
        "code_snippet": "#include <stdio.h>\nint main() {\n    int a = 5;\n    int b = a++ + ++a;\n    printf(\"%d\", b);\n    return 0;\n}",
        "option_a": "12",
        "option_b": "Undefined Behavior",
        "option_c": "10",
        "option_d": "11",
        "correct_option": "B",
        "explanation": "Modifying a variable multiple times without an intervening sequence point invokes Undefined Behavior in C/C++ standard specifications."
    },
    # 10. Medium - Java (Memory Management)
    {
        "category_id": 1,
        "difficulty": "MEDIUM",
        "question_text": "Since Java 8, which memory region replaces PermGen and is allocated out of native OS memory?",
        "code_snippet": None,
        "option_a": "Survivor Space",
        "option_b": "Metaspace",
        "option_c": "Eden Space",
        "option_d": "Old Generation",
        "correct_option": "B",
        "explanation": "Java 8 replaced PermGen with Metaspace, allowing class metadata to grow dynamically within native host RAM."
    },
    # 11. Medium - Python (Logical Reasoning)
    {
        "category_id": 1,
        "difficulty": "MEDIUM",
        "question_text": "What will be printed when running this Python script?",
        "code_snippet": "a = [1, 2, 3]\nb = a\nb += [4]\nprint(len(a))",
        "option_a": "3",
        "option_b": "4",
        "option_c": "TypeError",
        "option_d": "7",
        "correct_option": "B",
        "explanation": "For lists, '+=' executes in-place mutation (calling __iadd__ / extend). Both 'a' and 'b' refer to the same list instance, which now has 4 elements."
    },
    # 12. Hard - Java (Memory Model / Concurrency)
    {
        "category_id": 1,
        "difficulty": "HARD",
        "question_text": "Under the Java Memory Model (JMM), what happens-before guarantee does a write to a 'volatile' variable establish?",
        "code_snippet": None,
        "option_a": "A write to volatile is atomic only for 32-bit types",
        "option_b": "A write to volatile happens-before every subsequent read of that volatile variable",
        "option_c": "A write to volatile locks the enclosing class object",
        "option_d": "Volatile writes are buffered locally and flushed once per second",
        "correct_option": "B",
        "explanation": "The JMM specifies that a write to a volatile variable establishes a happens-before relationship with any subsequent read of that variable by any thread."
    },
    # 13. Hard - Python (Default Mutable Argument Bug)
    {
        "category_id": 1,
        "difficulty": "HARD",
        "question_text": "Predict the output of the following Python function calls:",
        "code_snippet": "def append_item(x, target=[]):\n    target.append(x)\n    return target\n\nprint(append_item(1), append_item(2))",
        "option_a": "[1] [2]",
        "option_b": "[1] [1, 2]",
        "option_c": "[1, 2] [1, 2]",
        "option_d": "TypeError: target is immutable",
        "correct_option": "C",
        "explanation": "Default arguments are evaluated once when the function definition is executed. Both calls modify and return the exact same persistent list."
    },
    # 14. Hard - C++ (Move Semantics / Rvalue)
    {
        "category_id": 1,
        "difficulty": "HARD",
        "question_text": "In modern C++11/14/17, what does std::move(obj) actually do at runtime?",
        "code_snippet": "std::string a = \"battle\";\nstd::string b = std::move(a);",
        "option_a": "Physically copies the string buffers into register memory",
        "option_b": "Performs an unconditional static_cast to an rvalue reference (T&&)",
        "option_c": "Destroys 'a' immediately before initializing 'b'",
        "option_d": "Locks the memory bus for atomic pointer reassignment",
        "correct_option": "B",
        "explanation": "std::move does not move anything at runtime; it performs a static cast to an rvalue reference (T&&), enabling overload resolution to select the move constructor."
    },
    # 15. Hard - C (Bitwise Pointer Arithmetic)
    {
        "category_id": 1,
        "difficulty": "HARD",
        "question_text": "What is the output of the following C program?",
        "code_snippet": "#include <stdio.h>\nint main() {\n    int arr[] = {10, 20, 30, 40, 50};\n    int *p = arr;\n    printf(\"%d\", *(p + 3) - *(p + 1));\n    return 0;\n}",
        "option_a": "10",
        "option_b": "20",
        "option_c": "30",
        "option_d": "2",
        "correct_option": "B",
        "explanation": "*(p + 3) yields arr[3] (40). *(p + 1) yields arr[1] (20). 40 - 20 = 20."
    },
    # 16. Hard - Java (Garbage Collection Internals)
    {
        "category_id": 1,
        "difficulty": "HARD",
        "question_text": "Which modern OpenJDK garbage collector uses colored pointers and load barriers to achieve sub-millisecond maximum pause times?",
        "code_snippet": None,
        "option_a": "Serial GC",
        "option_b": "Parallel GC",
        "option_c": "CMS (Concurrent Mark Sweep)",
        "option_d": "ZGC (Z Garbage Collector)",
        "correct_option": "D",
        "explanation": "ZGC achieves sub-millisecond pause times regardless of heap size (from megabytes to terabytes) using reference coloring and load barriers."
    },

    # =========================================================================
    # CATEGORY 2: Data Structures & Algorithms - 16 Questions
    # =========================================================================
    # 1. Easy
    {
        "category_id": 2,
        "difficulty": "EASY",
        "question_text": "What is the worst-case time complexity of searching for an element in an unsorted array of size N?",
        "code_snippet": None,
        "option_a": "O(1)",
        "option_b": "O(log N)",
        "option_c": "O(N)",
        "option_d": "O(N log N)",
        "correct_option": "C",
        "explanation": "In an unsorted array, linear search must examine every element in the worst case, requiring O(N) operations."
    },
    # 2. Easy
    {
        "category_id": 2,
        "difficulty": "EASY",
        "question_text": "Which data structure operates strictly on a First-In-First-Out (FIFO) principle?",
        "code_snippet": None,
        "option_a": "Stack",
        "option_b": "Queue",
        "option_c": "Priority Queue",
        "option_d": "Max Heap",
        "correct_option": "B",
        "explanation": "Queues maintain FIFO ordering: items are inserted at the rear and removed from the front in order of arrival."
    },
    # 3. Easy
    {
        "category_id": 2,
        "difficulty": "EASY",
        "question_text": "What is the average time complexity of finding a key in a balanced Binary Search Tree (AVL or Red-Black Tree)?",
        "code_snippet": None,
        "option_a": "O(1)",
        "option_b": "O(log N)",
        "option_c": "O(N)",
        "option_d": "O(N^2)",
        "correct_option": "B",
        "explanation": "A self-balancing BST restricts tree height to O(log N), guaranteeing logarithmic search, insertion, and deletion."
    },
    # 4. Easy
    {
        "category_id": 2,
        "difficulty": "EASY",
        "question_text": "In a singly linked list with head pointer only, what is the time complexity of deleting the last element?",
        "code_snippet": None,
        "option_a": "O(1)",
        "option_b": "O(log N)",
        "option_c": "O(N)",
        "option_d": "O(N^2)",
        "correct_option": "C",
        "explanation": "To delete the last node, you must traverse to the second-to-last node to update its next pointer to null, requiring O(N) steps."
    },
    # 5. Easy
    {
        "category_id": 2,
        "difficulty": "EASY",
        "question_text": "Which sorting algorithm is known for having a best-case time complexity of O(N) on an already sorted array?",
        "code_snippet": None,
        "option_a": "Merge Sort",
        "option_b": "Bubble Sort (with swapped flag) / Insertion Sort",
        "option_c": "Selection Sort",
        "option_d": "Quick Sort (naive)",
        "correct_option": "B",
        "explanation": "Insertion Sort and optimized Bubble Sort detect when no swaps are performed during the first pass, terminating in O(N) time."
    },
    # 6. Medium (Predict Output / Traversal)
    {
        "category_id": 2,
        "difficulty": "MEDIUM",
        "question_text": "Given a Binary Search Tree with nodes inserted in order: [10, 5, 15, 3, 7], what is its In-Order traversal?",
        "code_snippet": None,
        "option_a": "10, 5, 3, 7, 15",
        "option_b": "3, 5, 7, 10, 15",
        "option_c": "3, 7, 5, 15, 10",
        "option_d": "15, 10, 7, 5, 3",
        "correct_option": "B",
        "explanation": "In-Order traversal (Left -> Root -> Right) of any valid Binary Search Tree always outputs keys in strictly ascending sorted order."
    },
    # 7. Medium (Shortest Path Algorithm)
    {
        "category_id": 2,
        "difficulty": "MEDIUM",
        "question_text": "Which algorithm finds the single-source shortest path in a weighted graph with non-negative edge weights in O((V + E) log V) time?",
        "code_snippet": None,
        "option_a": "Breadth-First Search (BFS)",
        "option_b": "Dijkstra's Algorithm with Min-Heap",
        "option_c": "Floyd-Warshall Algorithm",
        "option_d": "Kruskal's Algorithm",
        "correct_option": "B",
        "explanation": "Dijkstra's algorithm using an adjacency list and min-priority queue achieves O((V + E) log V) for non-negative edge weights."
    },
    # 8. Medium (Worst-case QuickSort)
    {
        "category_id": 2,
        "difficulty": "MEDIUM",
        "question_text": "Under what condition does QuickSort degrade to its worst-case time complexity of O(N^2)?",
        "code_snippet": None,
        "option_a": "When elements are distinct and randomized",
        "option_b": "When partitions are maximally unbalanced (e.g. sorted array with first element as pivot)",
        "option_c": "When using Median-of-Three pivot selection",
        "option_d": "When array size is an exact power of 2",
        "correct_option": "B",
        "explanation": "If partitions produce subproblems of size 0 and N-1 at every recursive level, call stack depth becomes N, resulting in O(N^2) comparisons."
    },
    # 9. Medium (Find Bug / Stack with Queues)
    {
        "category_id": 2,
        "difficulty": "MEDIUM",
        "question_text": "What is the minimum number of standard single-ended FIFO queues required to implement a LIFO Stack?",
        "code_snippet": None,
        "option_a": "1",
        "option_b": "2",
        "option_c": "3",
        "option_d": "Impossible with queues",
        "correct_option": "B",
        "explanation": "A stack can be simulated using 2 queues: either by making push costly (transferring elements) or by making pop costly."
    },
    # 10. Medium (Graph Cycle Detection)
    {
        "category_id": 2,
        "difficulty": "MEDIUM",
        "question_text": "In a directed graph, which algorithm can detect cycles and produce a valid topological ordering?",
        "code_snippet": None,
        "option_a": "Prim's Algorithm",
        "option_b": "Kahn's Algorithm (in-degree BFS) or DFS with recursion stack tracking",
        "option_c": "Binary Search",
        "option_d": "Kadane's Algorithm",
        "correct_option": "B",
        "explanation": "Kahn's algorithm uses in-degree tracking via BFS. If the final ordered list contains fewer than V nodes, a cycle exists."
    },
    # 11. Medium (Logical Reasoning / Hashing)
    {
        "category_id": 2,
        "difficulty": "MEDIUM",
        "question_text": "What is the primary drawback of Linear Probing for open-address hash collision resolution?",
        "code_snippet": None,
        "option_a": "Requires double the pointer memory of separate chaining",
        "option_b": "Primary Clustering (long contiguous blocks of occupied buckets)",
        "option_c": "Cannot handle string keys",
        "option_d": "Causes infinite recursion during table resize",
        "correct_option": "B",
        "explanation": "Linear probing creates runs of occupied slots (primary clustering), which degrades average lookup and insertion performance to O(N)."
    },
    # 12. Hard (Dynamic Programming / Kadane)
    {
        "category_id": 2,
        "difficulty": "HARD",
        "question_text": "In Kadane's algorithm for finding the maximum contiguous subarray sum, what are the time and auxiliary space complexities?",
        "code_snippet": "int maxSubArray(int[] nums) {\n    int cur = nums[0], max = nums[0];\n    for (int i = 1; i < nums.length; i++) {\n        cur = Math.max(nums[i], cur + nums[i]);\n        max = Math.max(max, cur);\n    }\n    return max;\n}",
        "option_a": "Time: O(N log N), Space: O(N)",
        "option_b": "Time: O(N), Space: O(1)",
        "option_c": "Time: O(N^2), Space: O(1)",
        "option_d": "Time: O(N), Space: O(N)",
        "correct_option": "B",
        "explanation": "Kadane inspects each array element exactly once in a single linear scan while keeping track of current and maximum sum in O(1) space."
    },
    # 13. Hard (Disjoint Set Union / Inverse Ackermann)
    {
        "category_id": 2,
        "difficulty": "HARD",
        "question_text": "What is the amortized time complexity per operation in a Disjoint Set Union (Union-Find) structure with both union-by-rank and path compression?",
        "code_snippet": None,
        "option_a": "O(log N)",
        "option_b": "O(alpha(N)) where alpha is the Inverse Ackermann function",
        "option_c": "O(N)",
        "option_d": "O(sqrt(N))",
        "correct_option": "B",
        "explanation": "With both union-by-rank and path compression heuristics, any sequence of M operations on N elements runs in O(M * alpha(N)) time, which is practically constant."
    },
    # 14. Hard (Strongly Connected Components)
    {
        "category_id": 2,
        "difficulty": "HARD",
        "question_text": "Which algorithm computes all Strongly Connected Components (SCCs) of a directed graph in a single DFS pass in O(V + E) time?",
        "code_snippet": None,
        "option_a": "Tarjan's SCC Algorithm",
        "option_b": "Bellman-Ford Algorithm",
        "option_c": "Boruvka's Algorithm",
        "option_d": "Floyd-Warshall Algorithm",
        "correct_option": "A",
        "explanation": "Tarjan's algorithm uses discovery times and low-link values in a single DFS traversal with a stack to identify SCCs in linear O(V + E) time."
    },
    # 15. Hard (Binary Heap Indexing Math)
    {
        "category_id": 2,
        "difficulty": "HARD",
        "question_text": "In a 0-indexed array representation of a complete binary heap, what is the index of the parent of a node at index 'i' (where i > 0)?",
        "code_snippet": None,
        "option_a": "(i - 1) / 2",
        "option_b": "i / 2",
        "option_c": "(i + 1) / 2",
        "option_d": "2 * i + 1",
        "correct_option": "A",
        "explanation": "In 0-based array heaps, node 'i' has left child at 2i + 1, right child at 2i + 2, and parent at floor((i - 1) / 2)."
    },
    # 16. Hard (Trie / Prefix Trees)
    {
        "category_id": 2,
        "difficulty": "HARD",
        "question_text": "What is the time complexity to search for a word of length L in a Trie containing N words of average length L?",
        "code_snippet": None,
        "option_a": "O(N * L)",
        "option_b": "O(L)",
        "option_c": "O(log N)",
        "option_d": "O(L * log N)",
        "correct_option": "B",
        "explanation": "Searching in a Trie depends solely on the length of the query key (L), independent of the total number of words (N) stored."
    },

    # =========================================================================
    # CATEGORY 3: Database Management Systems - 16 Questions
    # =========================================================================
    # 1. Easy
    {
        "category_id": 3,
        "difficulty": "EASY",
        "question_text": "Which SQL clause is used to filter records resulting from an aggregate GROUP BY operation?",
        "code_snippet": "SELECT dept, COUNT(*) FROM emp GROUP BY dept ___ COUNT(*) > 5;",
        "option_a": "WHERE",
        "option_b": "HAVING",
        "option_c": "FILTER",
        "option_d": "LIMIT",
        "correct_option": "B",
        "explanation": "HAVING filters aggregated groups after the GROUP BY phase, whereas WHERE filters individual rows before grouping."
    },
    # 2. Easy
    {
        "category_id": 3,
        "difficulty": "EASY",
        "question_text": "What does the 'A' in ACID database transaction properties represent?",
        "code_snippet": None,
        "option_a": "Availability",
        "option_b": "Atomicity",
        "option_c": "Authentication",
        "option_d": "Asynchronous",
        "correct_option": "B",
        "explanation": "ACID stands for Atomicity (all-or-nothing), Consistency, Isolation, and Durability."
    },
    # 3. Easy
    {
        "category_id": 3,
        "difficulty": "EASY",
        "question_text": "Which Normal Form (NF) mandates that all non-key attributes are fully functionally dependent on the entire primary key (no partial dependencies)?",
        "code_snippet": None,
        "option_a": "First Normal Form (1NF)",
        "option_b": "Second Normal Form (2NF)",
        "option_c": "Third Normal Form (3NF)",
        "option_d": "Boyce-Codd Normal Form (BCNF)",
        "correct_option": "B",
        "explanation": "2NF requires 1NF and guarantees no partial dependencies: every non-prime attribute must depend on the whole candidate key."
    },
    # 4. Easy
    {
        "category_id": 3,
        "difficulty": "EASY",
        "question_text": "Which SQL statement is classified as Data Definition Language (DDL)?",
        "code_snippet": None,
        "option_a": "UPDATE",
        "option_b": "ALTER TABLE",
        "option_c": "INSERT INTO",
        "option_d": "DELETE FROM",
        "correct_option": "B",
        "explanation": "ALTER TABLE defines or modifies database schema structure and is classified as DDL. INSERT, UPDATE, and DELETE are DML."
    },
    # 5. Easy
    {
        "category_id": 3,
        "difficulty": "EASY",
        "question_text": "What is the primary role of a Foreign Key constraint in relational databases?",
        "code_snippet": None,
        "option_a": "To encrypt user passwords automatically",
        "option_b": "To enforce referential integrity between two related tables",
        "option_c": "To speed up Full-Text search queries",
        "option_d": "To force all text values into UPPERCASE",
        "correct_option": "B",
        "explanation": "A Foreign Key ensures referential integrity by enforcing that a column's value matches a primary key in another table."
    },
    # 6. Medium (B+ Tree Disk Indexing)
    {
        "category_id": 3,
        "difficulty": "MEDIUM",
        "question_text": "Why do relational database engines (like MySQL InnoDB) prefer B+ Trees over Binary Search Trees for disk-based indexing?",
        "code_snippet": None,
        "option_a": "B+ Trees hold fewer total keys per gigabyte",
        "option_b": "High fan-out yields low tree height, minimizing disk I/O operations",
        "option_c": "Binary trees cannot be saved to SSD storage",
        "option_d": "B+ Trees prevent all SQL injection attacks",
        "correct_option": "B",
        "explanation": "B+ Trees have hundreds or thousands of keys per page (high fan-out), keeping depth at 3-4 levels and requiring only a few disk block reads."
    },
    # 7. Medium (Transaction Isolation)
    {
        "category_id": 3,
        "difficulty": "MEDIUM",
        "question_text": "Which SQL transaction isolation level prevents Dirty Reads, Non-Repeatable Reads, and Phantom Reads?",
        "code_snippet": None,
        "option_a": "Read Uncommitted",
        "option_b": "Read Committed",
        "option_c": "Repeatable Read",
        "option_d": "Serializable",
        "correct_option": "D",
        "explanation": "Serializable is the highest isolation level, completely isolating concurrent transactions against all three read phenomena."
    },
    # 8. Medium (TRUNCATE vs DELETE)
    {
        "category_id": 3,
        "difficulty": "MEDIUM",
        "question_text": "What is the primary distinction between TRUNCATE and DELETE in SQL?",
        "code_snippet": None,
        "option_a": "DELETE drops table schema, TRUNCATE does not",
        "option_b": "TRUNCATE is a DDL operation deallocating data pages, while DELETE is DML logging row deletions",
        "option_c": "TRUNCATE triggers row-level AFTER DELETE triggers",
        "option_d": "DELETE cannot be rolled back in transactions",
        "correct_option": "B",
        "explanation": "TRUNCATE is DDL that resets table pages and high-water mark without firing row-level triggers, making it much faster than row-by-row DELETE."
    },
    # 9. Medium (Find Bug / SQL Join)
    {
        "category_id": 3,
        "difficulty": "MEDIUM",
        "question_text": "What type of JOIN returns all records from Table A, along with matching rows from Table B, filling missing rows with NULL?",
        "code_snippet": "SELECT * FROM TableA ___ JOIN TableB ON TableA.id = TableB.a_id;",
        "option_a": "INNER JOIN",
        "option_b": "LEFT OUTER JOIN",
        "option_c": "CROSS JOIN",
        "option_d": "NATURAL INNER JOIN",
        "correct_option": "B",
        "explanation": "A LEFT OUTER JOIN preserves all rows from the left table regardless of whether a matching record exists in the right table."
    },
    # 10. Medium (Two-Phase Locking)
    {
        "category_id": 3,
        "difficulty": "MEDIUM",
        "question_text": "In Strict Two-Phase Locking (Strict 2PL), when are exclusive locks (X-locks) held by a transaction released?",
        "code_snippet": None,
        "option_a": "Immediately after each SELECT query finishes",
        "option_b": "Only at the end of the transaction (COMMIT or ROLLBACK)",
        "option_c": "As soon as the shrinking phase begins",
        "option_d": "Whenever a foreign key check succeeds",
        "correct_option": "B",
        "explanation": "Strict 2PL holds all exclusive locks until transaction completion (commit/abort), preventing cascading aborts."
    },
    # 11. Medium (Indexing Nuances)
    {
        "category_id": 3,
        "difficulty": "MEDIUM",
        "question_text": "Given a composite index on (dept_id, salary), which query can fully utilize the index for filtering?",
        "code_snippet": "CREATE INDEX idx_dept_sal ON employees(dept_id, salary);",
        "option_a": "WHERE salary > 50000",
        "option_b": "WHERE dept_id = 10 AND salary > 50000",
        "option_c": "WHERE salary = 70000",
        "option_d": "WHERE YEAR(hire_date) = 2024",
        "correct_option": "B",
        "explanation": "Composite B-Tree indexes follow the leftmost-prefix rule: queries must filter on the leading column (dept_id) to leverage subsequent columns."
    },
    # 12. Hard (Clustered Index Internals)
    {
        "category_id": 3,
        "difficulty": "HARD",
        "question_text": "In MySQL InnoDB storage engine, what is stored in the leaf nodes of a secondary (non-clustered) index?",
        "code_snippet": None,
        "option_a": "Direct disk block offsets to physical cylinders",
        "option_b": "The primary key value of the row",
        "option_c": "The full complete row data duplicated",
        "option_d": "The transaction undo log pointer",
        "correct_option": "B",
        "explanation": "InnoDB secondary indexes store the indexed columns plus the corresponding Primary Key value, which is then used to traverse the clustered index."
    },
    # 13. Hard (CAP Theorem)
    {
        "category_id": 3,
        "difficulty": "HARD",
        "question_text": "According to Brewer's CAP Theorem, when a network partition (P) occurs in a distributed database, what trade-off must be made?",
        "code_snippet": None,
        "option_a": "Atomicity vs Durability",
        "option_b": "Consistency vs Availability",
        "option_c": "Encryption vs Compression",
        "option_d": "Latency vs Replication Factor",
        "correct_option": "B",
        "explanation": "When network partitions (P) happen, a distributed system must choose between returning consistent latest data (C) or responding to all requests (A)."
    },
    # 14. Hard (MVCC & Undo Logs)
    {
        "category_id": 3,
        "difficulty": "HARD",
        "question_text": "How does MySQL InnoDB implement Multi-Version Concurrency Control (MVCC) for non-locking consistent reads?",
        "code_snippet": None,
        "option_a": "By locking all tables in exclusive mode during reads",
        "option_b": "By combining Read Views with Undo Log record chains",
        "option_c": "By copying the entire database to RAM for each connection",
        "option_d": "By aborting any write that clashes with a read timestamp",
        "correct_option": "B",
        "explanation": "InnoDB builds consistent row snapshots on the fly by traversing roll_ptr links in Undo Log segments according to the transaction's Read View."
    },
    # 15. Hard (Write-Ahead Logging / WAL)
    {
        "category_id": 3,
        "difficulty": "HARD",
        "question_text": "What fundamental rule does the Write-Ahead Logging (WAL) protocol enforce before dirty pages are flushed to table data files?",
        "code_snippet": None,
        "option_a": "All table constraints must be dropped temporarily",
        "option_b": "Corresponding log records describing the update must be flushed to stable disk storage first",
        "option_c": "The database must wait for 60 seconds of zero network activity",
        "option_d": "Secondary indexes must be dropped and recreated",
        "correct_option": "B",
        "explanation": "WAL dictates that redo/undo log records must reach non-volatile disk before the corresponding modified data page can be written, guaranteeing Durability."
    },
    # 16. Hard (Normalization - BCNF vs 3NF)
    {
        "category_id": 3,
        "difficulty": "HARD",
        "question_text": "What condition must hold for every non-trivial functional dependency X -> Y in Boyce-Codd Normal Form (BCNF)?",
        "code_snippet": None,
        "option_a": "Y must be a prime attribute",
        "option_b": "X must be a superkey",
        "option_c": "Y must be a foreign key",
        "option_d": "X and Y must be numeric columns",
        "correct_option": "B",
        "explanation": "BCNF requires that for every functional dependency X -> Y, the determinant X must be a superkey of the relation."
    },

    # =========================================================================
    # CATEGORY 4: Operating Systems - 16 Questions
    # =========================================================================
    # 1. Easy
    {
        "category_id": 4,
        "difficulty": "EASY",
        "question_text": "Which process state occurs when an executing process issues an I/O request and awaits hardware completion?",
        "code_snippet": None,
        "option_a": "Ready",
        "option_b": "Blocked / Waiting",
        "option_c": "Terminated",
        "option_d": "Zombie",
        "correct_option": "B",
        "explanation": "A process transitions to the Blocked/Waiting state until the asynchronous I/O completion interrupt is serviced by the OS."
    },
    # 2. Easy
    {
        "category_id": 4,
        "difficulty": "EASY",
        "question_text": "What is the primary responsibility of the OS CPU scheduler Dispatcher?",
        "code_snippet": None,
        "option_a": "Translating C code to binary machine instructions",
        "option_b": "Performing context switching and giving CPU control to the selected process",
        "option_c": "Allocating physical disk sectors to swap partitions",
        "option_d": "Assigning IP addresses to Ethernet interfaces",
        "correct_option": "B",
        "explanation": "The dispatcher performs context switching, toggles the CPU to user mode, and jumps to the program counter of the chosen thread."
    },
    # 3. Easy
    {
        "category_id": 4,
        "difficulty": "EASY",
        "question_text": "Which of the following is NOT one of the four necessary Coffman conditions for Deadlock to occur?",
        "code_snippet": None,
        "option_a": "Mutual Exclusion",
        "option_b": "Hold and Wait",
        "option_c": "Preemption Allowed",
        "option_d": "Circular Wait",
        "correct_option": "C",
        "explanation": "The condition is NO PREEMPTION. If resources can be preempted forcibly, deadlock cannot exist."
    },
    # 4. Easy
    {
        "category_id": 4,
        "difficulty": "EASY",
        "question_text": "In Linux, which system call creates an exact duplicate child process sharing the parent's execution state?",
        "code_snippet": None,
        "option_a": "exec()",
        "option_b": "fork()",
        "option_c": "pthread_create()",
        "option_d": "signal()",
        "correct_option": "B",
        "explanation": "fork() creates a new child process with its own PID by duplicating the address space of the caller using copy-on-write."
    },
    # 5. Easy
    {
        "category_id": 4,
        "difficulty": "EASY",
        "question_text": "What is the smallest unit of execution that an operating system kernel schedules on a CPU core?",
        "code_snippet": None,
        "option_a": "Process",
        "option_b": "Thread",
        "option_c": "Interrupt Handler",
        "option_d": "Virtual Page",
        "correct_option": "B",
        "explanation": "A thread (lightweight process) is the smallest schedulable execution entity that shares memory space with peer threads in a process."
    },
    # 6. Medium (Thrashing)
    {
        "category_id": 4,
        "difficulty": "MEDIUM",
        "question_text": "What condition occurs when the OS spends more time swapping virtual memory pages than executing CPU instructions?",
        "code_snippet": None,
        "option_a": "Deadlock",
        "option_b": "Thrashing",
        "option_c": "Starvation",
        "option_d": "External Fragmentation",
        "correct_option": "B",
        "explanation": "Thrashing happens when the aggregate working sets of all active processes exceed physical RAM, causing constant page faults."
    },
    # 7. Medium (Zombie vs Orphan)
    {
        "category_id": 4,
        "difficulty": "MEDIUM",
        "question_text": "What is a 'Zombie Process' in Unix-like operating systems?",
        "code_snippet": None,
        "option_a": "A background daemon running forever",
        "option_b": "A terminated process whose parent has not yet collected its exit code via wait()",
        "option_c": "A process whose parent terminated while it was still executing",
        "option_d": "A process that consumed 100% of available CPU",
        "correct_option": "B",
        "explanation": "A zombie process has exited, but retains an entry in the kernel process table until its parent reads its exit status with wait()."
    },
    # 8. Medium (Convoy Effect)
    {
        "category_id": 4,
        "difficulty": "MEDIUM",
        "question_text": "Which CPU scheduling algorithm frequently suffers from the 'Convoy Effect'?",
        "code_snippet": None,
        "option_a": "Round Robin (RR)",
        "option_b": "First-Come, First-Served (FCFS)",
        "option_c": "Shortest Job First (Preemptive)",
        "option_d": "Multilevel Feedback Queue",
        "correct_option": "B",
        "explanation": "In FCFS, when a long CPU-burst process monopolizes the CPU, short I/O-bound processes queue behind it, reducing CPU and device utilization."
    },
    # 9. Medium (Inter-Process Communication)
    {
        "category_id": 4,
        "difficulty": "MEDIUM",
        "question_text": "Which IPC mechanism provides the fastest data exchange between processes running on the same physical host?",
        "code_snippet": None,
        "option_a": "Named Pipes (FIFOs)",
        "option_b": "Shared Memory (shmget / mmap)",
        "option_c": "UNIX Domain Sockets",
        "option_d": "Message Queues",
        "correct_option": "B",
        "explanation": "Shared memory maps the same physical RAM frames into multiple process address spaces, avoiding redundant kernel-space copying."
    },
    # 10. Medium (Predict Scheduling Output)
    {
        "category_id": 4,
        "difficulty": "MEDIUM",
        "question_text": "Three processes arrive at time 0 with burst times P1=24, P2=3, P3=3. Under FCFS (order P1, P2, P3), what is the average waiting time?",
        "code_snippet": "P1 wait = 0\nP2 wait = 24\nP3 wait = 24 + 3 = 27",
        "option_a": "17 ms",
        "option_b": "27 ms",
        "option_c": "15 ms",
        "option_d": "10 ms",
        "correct_option": "A",
        "explanation": "Waiting times: P1=0, P2=24, P3=27. Average = (0 + 24 + 27) / 3 = 51 / 3 = 17 ms."
    },
    # 11. Medium (Semaphore Math)
    {
        "category_id": 4,
        "difficulty": "MEDIUM",
        "question_text": "A counting semaphore S is initialized to 7. If 12 wait() (P) operations and 8 signal() (V) operations are performed, what is the final value of S?",
        "code_snippet": "Initial = 7\n12 * wait (decrement)\n8 * signal (increment)",
        "option_a": "1",
        "option_b": "3",
        "option_c": "0",
        "option_d": "-1",
        "correct_option": "B",
        "explanation": "Formula: Final = Initial - waitCount + signalCount = 7 - 12 + 8 = 3."
    },
    # 12. Hard (TLB & Virtual Memory)
    {
        "category_id": 4,
        "difficulty": "HARD",
        "question_text": "What is the primary function of the hardware Translation Lookaside Buffer (TLB)?",
        "code_snippet": None,
        "option_a": "To queue network TCP packets before sending",
        "option_b": "To cache virtual-to-physical page frame translations, avoiding multi-level RAM page table lookups",
        "option_c": "To buffer writes to SSD flash blocks",
        "option_d": "To store active process register states during sleep",
        "correct_option": "B",
        "explanation": "The TLB is an associative hardware cache that holds recent virtual-to-physical address mappings, speeding up memory accesses significantly."
    },
    # 13. Hard (Belady's Anomaly)
    {
        "category_id": 4,
        "difficulty": "HARD",
        "question_text": "Which page replacement algorithm can exhibit Belady's Anomaly (where increasing physical page frames can increase page faults)?",
        "code_snippet": None,
        "option_a": "LRU (Least Recently Used)",
        "option_b": "FIFO (First-In, First-Out)",
        "option_c": "Optimal Page Replacement (OPT)",
        "option_d": "LFU (Least Frequently Used with aging)",
        "correct_option": "B",
        "explanation": "FIFO is not a stack algorithm; certain reference strings generate more page faults when allocated more physical frames."
    },
    # 14. Hard (Peterson's Algorithm)
    {
        "category_id": 4,
        "difficulty": "HARD",
        "question_text": "In Peterson's Algorithm for two-process mutual exclusion, which two shared state variables are required?",
        "code_snippet": None,
        "option_a": "A counting semaphore and mutex pointer",
        "option_b": "A boolean array flag[2] and an integer turn variable",
        "option_c": "Two condition variables and a file lock",
        "option_d": "An atomic hardware compare-and-swap register",
        "correct_option": "B",
        "explanation": "Peterson's algorithm relies on flag[i] to signify interest in entering the critical section, and 'turn' to resolve simultaneous competition."
    },
    # 15. Hard (Banker's Algorithm)
    {
        "category_id": 4,
        "difficulty": "HARD",
        "question_text": "What fundamental data structures are maintained by Dijkstra's Banker's Algorithm for deadlock avoidance?",
        "code_snippet": None,
        "option_a": "Adjacency List and Priority Queue",
        "option_b": "Available vector, Max matrix, Allocation matrix, and Need matrix",
        "option_c": "Hash Table and Red-Black Tree",
        "option_d": "Ring buffer and Circular Linked List",
        "correct_option": "B",
        "explanation": "The Banker's Algorithm tracks Available[m], Max[n][m], Allocation[n][m], and Need[n][m] (where Need = Max - Allocation) to ensure safe states."
    },
    # 16. Hard (Inverted Page Table)
    {
        "category_id": 4,
        "difficulty": "HARD",
        "question_text": "What is the primary advantage of an Inverted Page Table over conventional multi-level hierarchical page tables?",
        "code_snippet": None,
        "option_a": "Eliminates the need for any hardware TLB",
        "option_b": "Memory overhead scales with physical RAM size rather than the aggregate virtual address spaces",
        "option_c": "Guarantees zero page faults at runtime",
        "option_d": "Translates addresses without CPU assistance",
        "correct_option": "B",
        "explanation": "An inverted page table has one entry per physical frame instead of per virtual page, drastically reducing page table memory footprint on 64-bit systems."
    },

    # =========================================================================
    # CATEGORY 5: Computer Networks - 16 Questions
    # =========================================================================
    # 1. Easy
    {
        "category_id": 5,
        "difficulty": "EASY",
        "question_text": "What is the default standard port number used for secure HTTPS web traffic?",
        "code_snippet": None,
        "option_a": "80",
        "option_b": "443",
        "option_c": "8080",
        "option_d": "22",
        "correct_option": "B",
        "explanation": "HTTPS runs over port 443 by default, whereas unencrypted HTTP operates over port 80."
    },
    # 2. Easy
    {
        "category_id": 5,
        "difficulty": "EASY",
        "question_text": "Which application layer protocol resolves human-friendly domain names (e.g. google.com) into IP addresses?",
        "code_snippet": None,
        "option_a": "DHCP",
        "option_b": "ARP",
        "option_c": "DNS",
        "option_d": "ICMP",
        "correct_option": "C",
        "explanation": "DNS (Domain Name System) translates textual hostnames to numerical IPv4/IPv6 addresses."
    },
    # 3. Easy
    {
        "category_id": 5,
        "difficulty": "EASY",
        "question_text": "Which layer of the OSI model provides end-to-end reliable data delivery, flow control, and error detection?",
        "code_snippet": None,
        "option_a": "Network Layer",
        "option_b": "Transport Layer",
        "option_c": "Data Link Layer",
        "option_d": "Physical Layer",
        "correct_option": "B",
        "explanation": "The Transport Layer (Layer 4, e.g., TCP) ensures reliable delivery, segment reordering, congestion control, and flow control."
    },
    # 4. Easy
    {
        "category_id": 5,
        "difficulty": "EASY",
        "question_text": "What is the standard size of an IPv4 address in bits and bytes?",
        "code_snippet": None,
        "option_a": "16 bits (2 bytes)",
        "option_b": "32 bits (4 bytes)",
        "option_c": "64 bits (8 bytes)",
        "option_d": "128 bits (16 bytes)",
        "correct_option": "B",
        "explanation": "An IPv4 address consists of 32 bits, typically represented as 4 dotted-decimal octets (e.g. 192.168.1.1)."
    },
    # 5. Easy
    {
        "category_id": 5,
        "difficulty": "EASY",
        "question_text": "Which network utility uses ICMP Echo Request and Echo Reply messages to test host reachability?",
        "code_snippet": None,
        "option_a": "traceroute",
        "option_b": "ping",
        "option_c": "netstat",
        "option_d": "nslookup",
        "correct_option": "B",
        "explanation": "'ping' sends ICMP Type 8 (Echo Request) packets and measures the round-trip latency when receiving ICMP Type 0 (Echo Reply)."
    },
    # 6. Medium (TCP 3-Way Handshake)
    {
        "category_id": 5,
        "difficulty": "MEDIUM",
        "question_text": "What is the exact sequence of control flags exchanged during the TCP Three-Way Handshake?",
        "code_snippet": None,
        "option_a": "ACK -> SYN -> SYN-ACK",
        "option_b": "SYN -> SYN-ACK -> ACK",
        "option_c": "FIN -> ACK -> FIN-ACK",
        "option_d": "SYN -> PSH -> ACK",
        "correct_option": "B",
        "explanation": "The client sends a SYN packet; the server replies with SYN-ACK; the client acknowledges with ACK to establish the connection."
    },
    # 7. Medium (ARP Role)
    {
        "category_id": 5,
        "difficulty": "MEDIUM",
        "question_text": "What is the primary role of the Address Resolution Protocol (ARP) in local area networks?",
        "code_snippet": None,
        "option_a": "Mapping a domain name to an IP address",
        "option_b": "Mapping a known IPv4 address to a physical MAC address on the local link",
        "option_c": "Encrypting payload packets for VPN tunnels",
        "option_d": "Allocating subnet masks dynamically",
        "correct_option": "B",
        "explanation": "ARP broadcasts on the local data link layer to find the physical Ethernet MAC address corresponding to a target IPv4 address."
    },
    # 8. Medium (TCP TIME_WAIT)
    {
        "category_id": 5,
        "difficulty": "MEDIUM",
        "question_text": "Why does the initiating endpoint in a TCP connection termination enter the TIME_WAIT state (2*MSL)?",
        "code_snippet": None,
        "option_a": "To renegotiate SSL/TLS encryption certificates",
        "option_b": "To ensure delayed duplicate packets drain from the network and retransmit the final ACK if lost",
        "option_c": "To compress socket buffers into gzip format",
        "option_d": "To force the operating system to reboot its network interface",
        "correct_option": "B",
        "explanation": "TIME_WAIT prevents duplicate segments from an old connection from corrupting a new connection and guarantees the peer received the final ACK."
    },
    # 9. Medium (Subnetting Calculation)
    {
        "category_id": 5,
        "difficulty": "MEDIUM",
        "question_text": "How many usable host IP addresses are available in a standard IPv4 /27 subnet?",
        "code_snippet": "Subnet mask: 255.255.255.224 (/27)",
        "option_a": "32",
        "option_b": "30",
        "option_c": "62",
        "option_d": "14",
        "correct_option": "B",
        "explanation": "A /27 subnet has 32 - 27 = 5 host bits. Total addresses = 2^5 = 32. Subtracting 2 (network address and broadcast address) gives 30 usable host IPs."
    },
    # 10. Medium (DHCP DORA Sequence)
    {
        "category_id": 5,
        "difficulty": "MEDIUM",
        "question_text": "What is the four-step message exchange sequence (DORA) used by DHCP to assign network configurations?",
        "code_snippet": None,
        "option_a": "Discover -> Offer -> Request -> Acknowledge",
        "option_b": "Deliver -> Open -> Receive -> Accept",
        "option_c": "Demand -> Order -> Route -> Assign",
        "option_d": "Detect -> Organize -> Replicate -> Approve",
        "correct_option": "A",
        "explanation": "DHCP follows DORA: Client sends Discover, Server replies with Offer, Client sends Request, Server responds with Acknowledge."
    },
    # 11. Medium (Logical Reasoning / Routing)
    {
        "category_id": 5,
        "difficulty": "MEDIUM",
        "question_text": "Which routing protocol uses the Dijkstra algorithm to calculate the shortest path tree within an Autonomous System (AS)?",
        "code_snippet": None,
        "option_a": "RIP (Routing Information Protocol)",
        "option_b": "OSPF (Open Shortest Path First)",
        "option_c": "BGP (Border Gateway Protocol)",
        "option_d": "EGP (Exterior Gateway Protocol)",
        "correct_option": "B",
        "explanation": "OSPF is a link-state routing protocol that distributes link-state advertisements and uses Dijkstra's algorithm to compute shortest paths."
    },
    # 12. Hard (TCP Congestion Control)
    {
        "category_id": 5,
        "difficulty": "HARD",
        "question_text": "In TCP congestion control, how does the congestion window (cwnd) grow during the Slow Start phase?",
        "code_snippet": None,
        "option_a": "Increases linearly by 1 MSS per RTT",
        "option_b": "Doubles exponentially every Round-Trip Time (RTT)",
        "option_c": "Triples on every incoming packet",
        "option_d": "Remains static until a timeout occurs",
        "correct_option": "B",
        "explanation": "In Slow Start, cwnd increments by 1 MSS for every received ACK, effectively doubling the congestion window every round-trip time."
    },
    # 13. Hard (HTTP/2 vs HTTP/1.1 Multiplexing)
    {
        "category_id": 5,
        "difficulty": "HARD",
        "question_text": "How does HTTP/2 eliminate the application-layer Head-of-Line (HoL) blocking found in HTTP/1.1?",
        "code_snippet": None,
        "option_a": "By using UDP instead of TCP",
        "option_b": "By framing messages into binary chunks multiplexed concurrently over a single TCP stream",
        "option_c": "By removing headers completely from requests",
        "option_d": "By opening a separate TCP connection for every image asset",
        "correct_option": "B",
        "explanation": "HTTP/2 introduces a binary framing layer that breaks requests/responses into discrete interleaved frames sent over a single TCP connection."
    },
    # 14. Hard (QUIC / HTTP/3 Architecture)
    {
        "category_id": 5,
        "difficulty": "HARD",
        "question_text": "What transport-layer protocol does HTTP/3 operate over to overcome TCP-level Head-of-Line blocking across packet loss?",
        "code_snippet": None,
        "option_a": "SCTP",
        "option_b": "QUIC (built on UDP)",
        "option_c": "RSVP",
        "option_d": "IPsec",
        "correct_option": "B",
        "explanation": "HTTP/3 uses QUIC running over UDP, where lost packets on one logical stream do not stall independent concurrent streams."
    },
    # 15. Hard (BGP Routing / Autonomous Systems)
    {
        "category_id": 5,
        "difficulty": "HARD",
        "question_text": "Why is BGP (Border Gateway Protocol) classified as a Path-Vector protocol rather than a pure Distance-Vector protocol?",
        "code_snippet": None,
        "option_a": "It uses Euclidean vector distances in kilometers",
        "option_b": "It advertises the complete sequence of Autonomous System Numbers (AS_PATH) to prevent routing loops",
        "option_c": "It only routes UDP multicast packets",
        "option_d": "It requires all routers on Earth to share a single master password",
        "correct_option": "B",
        "explanation": "BGP includes the AS_PATH attribute in routing advertisements, allowing routers to detect and drop updates that already contain their own AS number."
    },
    # 16. Hard (NAT / NAT Traversal)
    {
        "category_id": 5,
        "difficulty": "HARD",
        "question_text": "In WebRTC and real-time distributed networking, what do STUN and TURN protocols accomplish?",
        "code_snippet": None,
        "option_a": "Compile JavaScript to WebAssembly",
        "option_b": "Assist clients behind NAT/firewalls in discovering public IP/ports (STUN) or relaying media when P2P fails (TURN)",
        "option_c": "Balance MySQL database transactions across read replicas",
        "option_d": "Provide 4-way WPA3 WiFi handshakes",
        "correct_option": "B",
        "explanation": "STUN allows NATed clients to find their external IP:port mapping, while TURN acts as a fallback relay server when symmetric NAT blocks direct peer-to-peer connection."
    },

    # =========================================================================
    # CATEGORY 6: AI & Technology - 16 Questions
    # =========================================================================
    # 1. Easy
    {
        "category_id": 6,
        "difficulty": "EASY",
        "question_text": "What does the acronym 'LLM' stand for in modern Artificial Intelligence?",
        "code_snippet": None,
        "option_a": "Logical Learning Mechanism",
        "option_b": "Large Language Model",
        "option_c": "Linear Loss Matrix",
        "option_d": "Low-Latency Module",
        "correct_option": "B",
        "explanation": "LLM stands for Large Language Model, referring to deep neural networks trained on vast text corpora (e.g. GPT, Gemini, Claude)."
    },
    # 2. Easy
    {
        "category_id": 6,
        "difficulty": "EASY",
        "question_text": "Which activation function computes f(x) = max(0, x) and is widely used in deep neural network hidden layers?",
        "code_snippet": None,
        "option_a": "Sigmoid",
        "option_b": "ReLU (Rectified Linear Unit)",
        "option_c": "Tanh",
        "option_d": "Softmax",
        "correct_option": "B",
        "explanation": "ReLU outputs the input directly if positive, otherwise zero, mitigating the vanishing gradient problem in deep networks."
    },
    # 3. Easy
    {
        "category_id": 6,
        "difficulty": "EASY",
        "question_text": "What is the primary role of an API Gateway in a microservices and distributed computing architecture?",
        "code_snippet": None,
        "option_a": "Direct disk storage for raw audio files",
        "option_b": "Single entry point routing requests, handling security, authentication, and rate limiting",
        "option_c": "Generating synthetic training data for GANs",
        "option_d": "Compiling bytecode into machine code",
        "correct_option": "B",
        "explanation": "An API Gateway provides a centralized ingress point for client requests, handling routing, SSL termination, and rate limiting."
    },
    # 4. Easy
    {
        "category_id": 6,
        "difficulty": "EASY",
        "question_text": "Which type of machine learning involves an agent learning to make decisions by taking actions in an environment to maximize cumulative reward?",
        "code_snippet": None,
        "option_a": "Supervised Learning",
        "option_b": "Reinforcement Learning",
        "option_c": "Unsupervised Clustering",
        "option_d": "Self-Supervised Masking",
        "correct_option": "B",
        "explanation": "Reinforcement Learning (RL) trains agents using reward and penalty feedback in an environment to discover optimal policies."
    },
    # 5. Easy
    {
        "category_id": 6,
        "difficulty": "EASY",
        "question_text": "What is the primary security benefit of using HTTPS instead of plain HTTP?",
        "code_snippet": None,
        "option_a": "Makes web pages load without CSS",
        "option_b": "Encrypts communication between client and server using TLS, preventing eavesdropping and tampering",
        "option_c": "Guarantees 100% server uptime",
        "option_d": "Prevents client browsers from opening developer tools",
        "correct_option": "B",
        "explanation": "HTTPS encrypts the data transport using TLS, ensuring confidentiality, data integrity, and server authentication."
    },
    # 6. Medium (Transformer Self-Attention)
    {
        "category_id": 6,
        "difficulty": "MEDIUM",
        "question_text": "What core mechanism introduced in 'Attention Is All You Need' (Vaswani et al., 2017) allows Transformers to process sequences in parallel?",
        "code_snippet": None,
        "option_a": "Recurrent Hidden State Passing",
        "option_b": "Multi-Head Self-Attention",
        "option_c": "Convolutional Max-Pooling",
        "option_d": "Markov Decision Processes",
        "correct_option": "B",
        "explanation": "Self-attention computes relationships between all tokens in a sequence simultaneously using Query, Key, and Value matrices without recurrent loops."
    },
    # 7. Medium (Raft Consensus Protocol)
    {
        "category_id": 6,
        "difficulty": "MEDIUM",
        "question_text": "In distributed consensus protocols like Raft, what are the three possible states of any participating node?",
        "code_snippet": None,
        "option_a": "Master, Worker, Arbiter",
        "option_b": "Leader, Follower, Candidate",
        "option_c": "Proposer, Acceptor, Learner",
        "option_d": "Active, Standby, Terminated",
        "correct_option": "B",
        "explanation": "In Raft, a node is in one of three states: Follower (responds to RPCs), Candidate (runs for election), or Leader (handles client requests)."
    },
    # 8. Medium (RAG Architecture)
    {
        "category_id": 6,
        "difficulty": "MEDIUM",
        "question_text": "What is the primary operational flow of a Retrieval-Augmented Generation (RAG) system?",
        "code_snippet": None,
        "option_a": "Fine-tuning base neural network weights on every user query",
        "option_b": "Querying a vector database for relevant context embeddings and augmenting the prompt before LLM generation",
        "option_c": "Compressing text into zip files before passing to model",
        "option_d": "Executing SQL commands directly inside the GPU register file",
        "correct_option": "B",
        "explanation": "RAG converts user queries to embeddings, retrieves relevant chunks from a vector index, and feeds them as grounded context to the LLM."
    },
    # 9. Medium (Cybersecurity / CSRF)
    {
        "category_id": 6,
        "difficulty": "MEDIUM",
        "question_text": "What security mechanism effectively prevents Cross-Site Request Forgery (CSRF) attacks in modern web apps?",
        "code_snippet": None,
        "option_a": "Disabling TLS/SSL certificates",
        "option_b": "Anti-CSRF tokens (Synchronizer Token Pattern) and SameSite cookie attributes",
        "option_c": "Minifying JavaScript bundle files",
        "option_d": "Using Base64 encoding on query parameters",
        "correct_option": "B",
        "explanation": "Anti-CSRF tokens ensure requests originate from the legitimate site, while SameSite=Strict/Lax cookies prevent browsers from sending cookies on cross-origin requests."
    },
    # 10. Medium (Docker / Container Isolation)
    {
        "category_id": 6,
        "difficulty": "MEDIUM",
        "question_text": "Which two Linux kernel features provide process isolation and hardware resource limiting respectively for Docker containers?",
        "code_snippet": None,
        "option_a": "systemd and cron",
        "option_b": "Namespaces (isolation) and cgroups (resource limits)",
        "option_c": "iptables and swap",
        "option_d": "glibc and SELinux",
        "correct_option": "B",
        "explanation": "Namespaces isolate process trees, network, and mount points; Control Groups (cgroups) meter and throttle CPU, memory, and disk I/O."
    },
    # 11. Medium (Logical Reasoning / Overfitting)
    {
        "category_id": 6,
        "difficulty": "MEDIUM",
        "question_text": "When a machine learning model achieves 99.8% training accuracy but only 52.1% validation accuracy, what is happening and what is a remedy?",
        "code_snippet": None,
        "option_a": "Underfitting; add more layers and parameters",
        "option_b": "Overfitting; apply Dropout, L2 regularization, or gather more diverse data",
        "option_c": "Gradient explosion; double the learning rate",
        "option_d": "Quantization loss; convert FP16 to INT4",
        "correct_option": "B",
        "explanation": "High training accuracy with poor validation accuracy is the textbook symptom of overfitting (memorizing noise), remedied by regularization and data augmentation."
    },
    # 12. Hard (Vector Search / HNSW)
    {
        "category_id": 6,
        "difficulty": "HARD",
        "question_text": "Why is Hierarchical Navigable Small World (HNSW) graphs widely preferred for production vector databases over brute-force kNN?",
        "code_snippet": None,
        "option_a": "HNSW converts vectors into SQL relational tables",
        "option_b": "It enables Approximate Nearest Neighbor (ANN) search across millions of dense embeddings in O(log N) time",
        "option_c": "It forces all embeddings to have unit length 1.0",
        "option_d": "It removes the need for GPU accelerators during training",
        "correct_option": "B",
        "explanation": "Brute-force search is O(N * D) which is intractable at scale. HNSW creates multi-layer skip-list graphs allowing sub-linear logarithmic ANN search."
    },
    # 13. Hard (Java RMI Distributed Architecture)
    {
        "category_id": 6,
        "difficulty": "HARD",
        "question_text": "In Java RMI distributed architecture, what is the precise role of the client-side 'Stub' object?",
        "code_snippet": None,
        "option_a": "It compiles the Java bytecode directly inside client memory",
        "option_b": "It acts as a local proxy that marshals parameters, initiates socket communication with the remote skeleton, and unmarshals return values",
        "option_c": "It connects directly to MySQL via JDBC",
        "option_d": "It provides WebSocket handshake negotiation for React browsers",
        "correct_option": "B",
        "explanation": "The RMI stub implements the remote interface on the client, transparently serializing parameters over network sockets to the remote server and returning results."
    },
    # 14. Hard (Amdahl's Law)
    {
        "category_id": 6,
        "difficulty": "HARD",
        "question_text": "According to Amdahl's Law, if 20% of a program is strictly serial (P = 0.8), what is the maximum theoretical speedup limit regardless of how many parallel processor cores are added?",
        "code_snippet": "Speedup = 1 / ((1 - P) + P / N)\nSerial fraction (1 - P) = 0.2",
        "option_a": "2x",
        "option_b": "5x",
        "option_c": "10x",
        "option_d": "Infinite speedup",
        "correct_option": "B",
        "explanation": "As N -> infinity, P / N -> 0. Maximum speedup = 1 / (1 - P) = 1 / 0.2 = 5x."
    },
    # 15. Hard (LLM Prompt Injection)
    {
        "category_id": 6,
        "difficulty": "HARD",
        "question_text": "In AI security, what is an 'Indirect Prompt Injection' attack against an LLM-powered application?",
        "code_snippet": None,
        "option_a": "Overclocking the GPU until weights overflow",
        "option_b": "Adversarial instructions embedded in untrusted external data (e.g. web pages, emails) that hijack the model's control flow when processed",
        "option_c": "Guessing the API key through brute force dictionary attack",
        "option_d": "Extracting model weights by measuring electromagnetic radiation",
        "correct_option": "B",
        "explanation": "Indirect prompt injection occurs when an LLM reads external untrusted content (like a webpage or document) containing malicious text that overrides system instructions."
    },
    # 16. Hard (Zero-Knowledge Proofs)
    {
        "category_id": 6,
        "difficulty": "HARD",
        "question_text": "What fundamental property defines a Zero-Knowledge Proof (ZKP) in modern cryptography?",
        "code_snippet": None,
        "option_a": "The proof is completely unencrypted and public",
        "option_b": "A prover can prove to a verifier that a statement is true without revealing any secret information beyond the validity of the statement itself",
        "option_c": "The algorithm requires zero CPU clock cycles to verify",
        "option_d": "The key must be shared across all nodes simultaneously",
        "correct_option": "B",
        "explanation": "A Zero-Knowledge Proof guarantees Completeness, Soundness, and Zero-Knowledge: proving truth without exposing the underlying secret witness."
    }
]

def escape_sql(val):
    if val is None:
        return "NULL"
    escaped = val.replace("\\", "\\\\").replace("'", "''")
    return f"'{escaped}'"

def generate_sql():
    lines = []
    lines.append("-- DevClash Tech Battle Initial Question Bank")
    lines.append("-- 96 Curated Questions (16 per category across EASY, MEDIUM, HARD)")
    lines.append("-- Supports: Standard MCQs, Code Output Prediction, Find the Bug, Concept, and Logical Reasoning")
    lines.append("USE devclash_db;\n")

    # Clear existing questions to prevent duplicates and seed fresh
    lines.append("-- Clean existing questions")
    lines.append("DELETE FROM questions;\n")
    lines.append("ALTER TABLE questions AUTO_INCREMENT = 1;\n")

    lines.append("INSERT INTO questions (category_id, difficulty, question_text, code_snippet, option_a, option_b, option_c, option_d, correct_option, explanation) VALUES")

    row_strs = []
    for q in questions:
        row = f"({q['category_id']}, '{q['difficulty']}', {escape_sql(q['question_text'])}, {escape_sql(q['code_snippet'])}, {escape_sql(q['option_a'])}, {escape_sql(q['option_b'])}, {escape_sql(q['option_c'])}, {escape_sql(q['option_d'])}, '{q['correct_option']}', {escape_sql(q['explanation'])})"
        row_strs.append(row)

    lines.append(",\n".join(row_strs) + ";\n")
    return "\n".join(lines)

if __name__ == "__main__":
    out_dir = r"c:\Users\Sejal Pc\Desktop\DevClash\database"
    sql_path = os.path.join(out_dir, "seed_questions.sql")
    content = generate_sql()
    with open(sql_path, "w", encoding="utf-8") as f:
        f.write(content)
    print(f"Generated {len(questions)} questions in {sql_path}")
