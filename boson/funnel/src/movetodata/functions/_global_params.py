class GlobalParams:
    def __init__(self):
        self._current_sources = None

    def setCurrentSources(self, sources):
        self._current_sources = sources

    def getCurrentSources(self):
        return self._current_sources
