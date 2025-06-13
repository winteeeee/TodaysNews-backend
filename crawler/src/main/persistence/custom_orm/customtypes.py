import io
import pickle
import numpy as np
from sqlalchemy import TypeDecorator, BLOB


class Vector(TypeDecorator):
    impl = BLOB

    def process_bind_param(self, value, dialect) -> bytes:
        with io.BytesIO() as memfile:
            np.save(memfile, value)
            serialized = memfile.getvalue()
        return serialized

    def process_result_value(self, value, dialect) -> np.ndarray:
        try:
            with io.BytesIO() as memfile:
                memfile.write(value)
                memfile.seek(0)
                vec = np.load(memfile)
        except:
            vec = None
        return vec


class PyObject(TypeDecorator):
    impl = BLOB

    def process_bind_param(self, value, dialect) -> bytes:
        with io.BytesIO() as memfile:
            pickle.dump(value, memfile)
            serialized = memfile.getvalue()
        return serialized

    def process_result_value(self, value, dialect):
        try:
            with io.BytesIO() as memfile:
                memfile.write(value)
                memfile.seek(0)
                value = pickle.load(memfile)
        except:
            value = None
        return value
