# BoxFS

![check workflow](https://github.com/avrong/boxfs/actions/workflows/check.yaml/badge.svg)

Single-file container for files.

## Overview

The internals of library are structured in levels (from highest to lowest):
 - BoxFs (operates with files and dirs)
 - Container (operates with blocks and Space)
 - Block and derived classes (operates with bytes and RangedSpace)

Each type of block has its type and size saved. Sizes of blocks are arbitrary, they are assigned and allocated on their creation and can't be changed later.

Blocks represent types of object parts saved in container:
 - SymbolBlock (for names)
 - FileBlock (for files) 
 - DirectoryBlock (for directories)
 - FirstBlock (for container metadata: currently only rootDir)

DirectoryBlock and FileBlock have a `next` param, so that they can be chained together when current block cannot fit any more data. On the other hand, when SymbolBlock cannot fit a new name (if rename happened), then the new one with a size of a new name is created.

SymbolBlock: 
```
type size occupied name...
byte int  long     string
```

DirectoryBlock:
```
type size next occupied entries(name, block offset)...
byte int  long int      <long, long>
```

FileBlock:
```
type size next occupied blob...
byte int  long long     bytes
```

FirstBlock:
```
type size rootdir
byte int  long
```

Container operates on Space, it can get blocks using offset and allocate new ones from the last byte container. For blocks, it creates RangedSpace to make writes with block bounds checks.

BoxFs operates on blocks and implements methods and underlying algorithms to provide filesystem interface to the library users.

### Possible improvements:
 - Use unsigned types for all numbers in block parameters
 - Memory mapping using sectors (addressing using sector:offset, blocks may be aligned with sector start)
 - In-place compaction / allocation in free space in the middle of file
 - File abstraction, flag for open files (blocked from editing regions)
 - Directory entries can be retrieved one-by-one in an iterator
 - Magic number, file version write and check

## Operations

 - exists(path: BoxPath): Boolean
 - isDirectory(path: BoxPath): Boolean
 - isFile(path: BoxPath): Boolean
 - move(pathFrom: BoxPath, pathTo: BoxPath): Boolean
 - delete(path: BoxPath): Boolean
 - rename(pathFrom: BoxPath, pathTo: BoxPath): Boolean
 - copy(pathFrom: BoxPath, pathTo: BoxPath): Boolean
 - getVisualTree(dirPath: BoxPath): String

### Directories
 - createDirectory(path: BoxPath): Boolean
 - createDirectories(path: BoxPath): Boolean
 - listDirectory(path: BoxPath): List<BoxPath>?
 - visitFileTree(dirPath: BoxPath, visitor: BoxFsVisitor)

### Files
 - createFile(path: BoxPath): Boolean
 - writeFile(path: BoxPath, byteArray: ByteArray): Boolean
 - appendFile(path: BoxPath, byteArray: ByteArray): Boolean
 - readFile(path: BoxPath): ByteArray?
 - getFileSize(path: BoxPath): Int?

### Working with external file system
 - populate(path: Path, internalPath: BoxPath)
 - materialize(internalDirPath: BoxPath, outputDirPath: Path)
 - compact()
