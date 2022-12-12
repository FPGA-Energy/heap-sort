
BUILD_DIR := build
TEST_FILE_DIR := src/test-files
MAIN_C_FILE := src/main/c/heap.c

TEST_FILES := $(wildcard $(TEST_FILE_DIR)/*K-sorted.txt)
TESTS := $(basename $(notdir $(TEST_FILES)))
K_VALUES := 2 4 8 16 32 64


C_REPETITIONS := 100
C_FILES := $(foreach test, $(TESTS), $(BUILD_DIR)/c_files/$(test).heapsort.c)
EXES := $(foreach k, $(K_VALUES), $(foreach test, $(TESTS), $(BUILD_DIR)/executables/$(test)_$(k).heapsort.out))


TOP=HeapSort
PART = xc7a35tcpg236-1
CONFIG_PART = xc7a35t_0
VIVADO_ARGS = -nojournal
CONSTRAINT := src/constraints/cmod-a7/pinout.xdc
SCALA_FILES := $(wildcard src/main/scala/**/*)
VERILOG_FILES := $(foreach k, $(K_VALUES), $(foreach test, $(TESTS), $(BUILD_DIR)/verilog/$(test)_$(k).heapsort.v))
BIT_STREAMS := $(foreach k, $(K_VALUES), $(foreach test, $(TESTS), $(BUILD_DIR)/bitstreams/$(test)_$(k).heapsort.bit))
SYNTH_TCL := $(foreach k, $(K_VALUES), $(foreach test, $(TESTS), $(BUILD_DIR)/$(test)_$(k)/synth.tcl))

executables: $(EXES)

verilog: $(VERILOG_FILES)
bitstreams: $(BIT_STREAMS)

$(BUILD_DIR)/c_files/%.heapsort.c: $(TEST_FILE_DIR)/%.txt $(BUILD_DIR) $(MAIN_C_FILE)
	@mkdir -p $(BUILD_DIR)/c_files
	@echo -n "" > $@
	@echo -n "int a[] = {" >> $@
	sed -e 's/.*/0x&,/' $< | sed ':a;N;$$!ba;s/\n/ /g' | sed '$$s/.$$//' >> $@
	@echo -n "};" >> $@
	tail -n 45 $(MAIN_C_FILE) >> $@

$(BUILD_DIR):
	mkdir build
	mkdir build/c_files build/executables build/verilog build/bitstreams

clean:
	rm -r build

define exe
$(BUILD_DIR)/executables/$(test)_$(k).heapsort.out: $(BUILD_DIR)/c_files/$(test).heapsort.c $(MAKEFILE)
	@mkdir -p $(BUILD_DIR)/executables
	gcc -O3 -o $(BUILD_DIR)/executables/$(test)_$(k).heapsort.out $(BUILD_DIR)/c_files/$(test).heapsort.c -DK=$(k) -DREPETITIONS=$(C_REPETITIONS)
endef

$(foreach k,$(K_VALUES), $(foreach test,$(TESTS), $(eval $(exe))))


define verilog_gen
$(BUILD_DIR)/verilog/$(test)_$(k).heapsort.v: $(SCALA_FILES) | $(BUILD_DIR)
	sbt "runMain $(TOP) --target-dir $(BUILD_DIR)/$(test)_$(k) --test-file $(TEST_FILE_DIR)/$(test).txt -k $(k) -w 32"
	mv $(BUILD_DIR)/$(test)_$(k)/$(TOP).v $(BUILD_DIR)/verilog/$(test)_$(k).heapsort.v
endef

$(foreach k,$(K_VALUES), $(foreach test,$(TESTS), $(eval $(verilog_gen))))

define bitstream_gen
$(BUILD_DIR)/bitstreams/$(test)_$(k).heapsort.bit: $(BUILD_DIR)/verilog/$(test)_$(k).heapsort.v $(BUILD_DIR)/$(test)_$(k)/synth.tcl
	vivado $(VIVADO_ARGS) -mode batch -tempDir $(BUILD_DIR)/$(test)_$(k) -log $(BUILD_DIR)/$(test)_$(k)/vivado.log -source $(BUILD_DIR)/$(test)_$(k)/synth.tcl
	@rm usage_statistics_webtalk.html usage_statistics_webtalk.xml
endef

$(foreach k,$(K_VALUES), $(foreach test,$(TESTS), $(eval $(bitstream_gen))))

define synth_tcl_gen
$(BUILD_DIR)/$(test)_$(k)/synth.tcl: $(MAKEFILE) | $(BUILD_DIR)
	@mkdir -p $(BUILD_DIR)/$(test)_$(k)
	echo -e "\
    read_verilog [ glob ./$(BUILD_DIR)/verilog/$(test)_$(k).heapsort.v ]\n\
    read_xdc ./$(CONSTRAINT)\n\
    synth_design -top $(TOP) -part $(PART)\n\
    opt_design\n\
    place_design\n\
    route_design\n\
    write_bitstream $(BUILD_DIR)/bitstreams/$(test)_$(k).heapsort.bit -force" > $(BUILD_DIR)/$(test)_$(k)/synth.tcl
endef

$(foreach k,$(K_VALUES), $(foreach test,$(TESTS), $(eval $(synth_tcl_gen))))
