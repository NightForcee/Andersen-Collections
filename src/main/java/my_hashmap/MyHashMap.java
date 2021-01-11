package my_hashmap;

import java.util.Objects;

public class MyHashMap<K, V> {
    static final int DEFAULT_INITIAL_CAPACITY = 16;   //стандартная емкость таблицы
    static final int MAXIMUM_CAPACITY = 1 << 30;      //максимально возможная емкость таблицы - +- 1 млн.
    static final float DEFAULT_LOAD_FACTOR = 0.75f;   // стандартный коэффициент загрузки
    transient Entry<K, V>[] table;                     // сама хеш-таблица, основана на массиве.для хранения пар ключ-значение.
    transient int size;                               // колличество пар ключ-значение
    int threshold;                                    //предельное кол-во эл-в, при переполнении -> увеличивается вдвое (capacity*loadFactor)
    private final float loadFactor;                 //коэфф загрузки
    private Entry<K, V> entry;                   // для переберания мапы.

    public MyHashMap() { //по умолчанию
        size = 0;
        loadFactor = DEFAULT_LOAD_FACTOR;
        threshold = Math.round(DEFAULT_INITIAL_CAPACITY * loadFactor);
        table = new Entry[DEFAULT_INITIAL_CAPACITY];
    }

    public MyHashMap(int initialCapacity, float loadFactor) { // указываем размер хеш-таблицы, коэфф загрузки
        size = 0;
        this.loadFactor = loadFactor;
        threshold = Math.round(initialCapacity * loadFactor);
        table = new Entry[initialCapacity];
    }

    static final int hash(Object key) {  //хеш-функиця
        int h;
        return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
    }

    public void put(K key, V value) { //метод добавить значение
        if (key == null) {
            putNull(value);
            return;
        }
        int hash = key.hashCode();  //генерируем хеш-код ключа
        int index = indexForBucket(hash, table.length); //с помощью метода,определяем индекс бакета(корзины)

        Entry<K, V> entry = table[index]; // присваиваем таблице индекс
        while (entry != null) { //если таблица не налл
            if (entry.hash == hash && (entry.key == key || key.equals(entry.key))) {  //производим проверку по хеш-функции,ключу
                entry.value = value; // присваиваем значение по данному ключу
                return;
            }
            entry = entry.next; // присваиваем следующий эл-т ( если такой имеется)пока не дойдет до нала
        }
        if (size + 1 >= threshold) { // если пар ключ-значение >= предельному заполнению
            resize(table.length * 2); // создаем новую хеш-таблицу
        }
        size++;  // если прошли иф, просто увеличиваем размер на +1
        addEntry(hash, key, value, index); // добавляем с помощью метода новую хеш-таблицу
    }

    private void putNull(V value) {  // особый метод, при ключ - нал
        Entry entry = table[0]; // присваиваем индекс 0

        while (entry != null) { // проводим проверку, если хеша не налл
            if (entry.key == null) { // если ключ налл
                entry.value = value; // присваиваем значение
                return;
            }
            entry = entry.next;
        }
        addEntry(0, null, value, 0);
    }

    public V get(K key) {  //получаем значение по ключу, в хеш-таблице
        Entry<K, V> entry;
        if (key == null) { //если ключ = налл
            entry = table[0];  // присваиваем таблице 0й индекс

            while (entry != null) {
                if (entry.key == null) {
                    return entry.value;
                }
                entry = entry.next;
            }
        } else {
            entry = table[indexForBucket(key.hashCode(), table.length)]; // если ключ не равен налл, через метод ищем индекс бакета
        }
        while (entry != null) {
            if (key.equals(entry.key)) {
                return entry.value;
            }
            entry = entry.next;
        }
        return null;
    }

    public boolean remove(K key) { // удаление по ключу
        Entry<K, V> entry;
        if (key == null) {
            entry = table[0];

            while (entry != null) {
                if (entry.key == null) {
                    table[0] = entry.next; // ищем последний элемент
                }
            }
            this.entry = entry.next;
        } else {
            entry = table[indexForBucket(key.hashCode(), table.length)];
        }

        while (entry != null) {
            if (key.equals(entry.key)) {
                table[indexForBucket(key.hashCode(), table.length)] = entry.next;
                return true;
            }
        }
        entry = entry.next;
        return true;
    }

    private void resize(int newCap) {  // изменение размера
        Entry<K, V>[] oldTable = table;
        int oldCapacity = (oldTable == null) ? 0 : oldTable.length; // тернарный оператор
        if (oldCapacity >= MAXIMUM_CAPACITY) {
            threshold = Integer.MAX_VALUE;
            return;
        }

        Entry<K, V>[] newEntry = new Entry[newCap];
        transfer(newEntry);
        threshold = Math.round(newCap * loadFactor); //round-возвращает целое число, ближайшее к вещественному,
    }

    void addEntry(int hash, K key, V value, int index) {
        if (++size > threshold) {
            resize(table.length * 2);
        }
        Entry<K, V> entry = table[index];
        table[index] = new Entry<>(hash, key, value, entry);
    }

    private void transfer(Entry<K, V>[] newTable) { // спец мето, для новой хеш-таблицы,которая приняла новый размер ( старый * 2)
        Entry<K, V>[] oldTable = table;
        table = newTable;
        for (Entry<K, V> table : oldTable) {
            while (table != null) {
                addEntry(table.hash, table.key, table.value, indexForBucket(table.hash, newTable.length));
                table = table.next;
            }
        }
    }

    static int indexForBucket(int hash, int length) {  // метод ,для поиска индекса по хеш-функции и длины таблицы
        return (length - 1) & hash;
    }

    static class Entry<K, V> {
        int hash;
        final K key;
        V value;
        Entry<K, V> next;

        public Entry(int hash, K key, V value, Entry<K, V> next) {
            this.hash = hash;
            this.key = key;
            this.value = value;
            this.next = next;
        }

        public int getHash() {
            return hash;
        }

        public K getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }

        public void setValue(V value) {
            this.value = value;
        }

        public Entry<K, V> getNext() {
            return next;
        }

        public void setNext(Entry<K, V> next) {
            this.next = next;
        }

        public final String toString() {
            return key + "=" + value;
        }

        public final int hashCode() {
            return Objects.hashCode(key) ^ Objects.hashCode(value);
        }

        public final boolean equals(Object o) {
            if (o == this)
                return true;
            if (o instanceof MyHashMap.Entry) {
                Entry<?, ?> e = (Entry<?, ?>) o;
                return Objects.equals(key, e.getKey()) &&
                        Objects.equals(value, e.getValue());
            }
            return false;
        }
    }

    public static void main(String[] args) {
        MyHashMap<String, Integer> hashMap = new MyHashMap<>(16, 0.75f);

        hashMap.put("Artem", 777);
        hashMap.put("Pavel", 666);
        hashMap.put("Arnold", 555);

        System.out.println(hashMap.get("Artem"));
        System.out.println(hashMap.get("Pavel"));
        System.out.println(hashMap.get("Arnold"));

        hashMap.remove("Arnold");
        hashMap.put("Artem", 228);

        System.out.println(hashMap.get("Arnold"));
        System.out.println(hashMap.get("Artem"));
    }
}
