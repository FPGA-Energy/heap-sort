

BUILD_DIR := build
TEST_FILE_DIR := src/test-files
MAIN_C_FILE := src/main/c/heap.c

REPETITIONS := 10

TEST_FILES := $(wildcard $(TEST_FILE_DIR)/*)
TESTS := $(basename $(notdir $(TEST_FILES)))
K_VALUES := 2 4 8 16 32 64 128
C_FILES := $(foreach test, $(TESTS), $(BUILD_DIR)/$(test).heapsort.c)
EXES := $(foreach k, $(K_VALUES), $(foreach test, $(TESTS), $(BUILD_DIR)/$(test)_$(k).heapsort.out))

executables: $(EXES)

$(BUILD_DIR)/%.heapsort.c: $(TEST_FILE_DIR)/%.txt $(BUILD_DIR)
	echo -n "" > $@ 
	echo -n "int a[] = {" >> $@
	sed -e 's/.*/0x&,/' $< | sed ':a;N;$$!ba;s/\n/ /g' | sed '$$s/.$$//' >> $@
	echo -n "};" >> $@
	tail -n 46 $(MAIN_C_FILE) >> $@

$(BUILD_DIR):
	mkdir build

clean:
	rm -r build

define exe
$(BUILD_DIR)/$(test)_$(k).heapsort.out: $(BUILD_DIR)/$(test).heapsort.c
	gcc -o $(BUILD_DIR)/$(test)_$(k).heapsort.out $(BUILD_DIR)/$(test).heapsort.c -DK=$(k) -DREPETITIONS=$(REPETITIONS)
endef

$(foreach k,$(K_VALUES), $(foreach test,$(TESTS), $(eval $(exe))))