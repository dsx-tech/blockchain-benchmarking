package chaincode

import (
"fmt"
"errors"
"github.com/hyperledger/fabric/core/chaincode/shim"
"crypto/sha256"
"encoding/base64"
)

type SimpleChaincode struct {

}

func (t *SimpleChaincode) Init(stub shim.ChaincodeStubInterface, function string, args []string) ([]byte, error) {

	if len(args) != 0 {
		return nil, errors.New("Incorrect number of arguments. Expecting 0")
	}

	return nil, nil
}

func (t *SimpleChaincode) Invoke(stub shim.ChaincodeStubInterface, function string, args []string) ([]byte, error) {
	fmt.Println("invoke is running " + function)

	if function == "init" {
		return t.Init(stub, function, args)
	} else if function == "write" {
		return t.Write(stub, args)
	}
	fmt.Println("invoke did not find func: " + function)

	return nil, errors.New("Received unknown function invocation")
}

func (t *SimpleChaincode) Write(stub shim.ChaincodeStubInterface, args []string) ([]byte, error) {

	return nil, nil
}

func (t *SimpleChaincode) Query(stub shim.ChaincodeStubInterface, function string, args []string) ([]byte, error)  {

	fmt.Println("query is running " + function)

	if function == "read" {
		return t.read(stub, args)
	}
	fmt.Println("query did not find func: " + function)

	return nil, errors.New("Received unknown query function")
}

func (t *SimpleChaincode) read(stub shim.ChaincodeStubInterface, args []string) ([]byte, error) {
	var err error
	var messagesCounter int64

	if len(args) != 0 {
		return nil, errors.New("Incorrect number of arguments. Expecting nothing")
	}

	keysIter, err := stub.RangeQueryState("", "")
	if err != nil {
		return nil, fmt.Errorf("keys operation failed. Error accessing state: %s", err)
	}
	defer keysIter.Close()
	var data = ""
	for keysIter.HasNext() {
		_, val, iterErr := keysIter.Next()
		messagesCounter = messagesCounter + 1
		if iterErr != nil {
			return nil, fmt.Errorf("keys operation failed. Error accessing state: %s", err)
		}
		strVal := string(val)
		if data == "" {
			data = strVal
		} else {
			data = fmt.Sprintf("%s`0%s", data, strVal)
		}
	}
	fmt.Println("Amount of messages in state: %d", messagesCounter)
	return []byte(data), nil
}

func getHash(data string) string {
	h := sha256.New()
	h.Write([]byte(data))
	b := h.Sum(nil)
	return base64.StdEncoding.EncodeToString(b)
}


func main() {
	err := shim.Start(new(SimpleChaincode))
	if err != nil {
		fmt.Printf("Error starting Simple chaincode: %s", err)
	}
}