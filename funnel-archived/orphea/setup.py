import setuptools

# with open("README.md", "r", encoding="utf-8") as fh:
#     long_description = fh.read()

setuptools.setup(
    name="orpheafunnel",
    version="0.0.1",
    author="Rakesh Malik",
    author_email="rakesh.malik@orphea.io",
    description="This is python orphea decorators.",
    long_description="long_description",
    long_description_content_type="text/markdown",
    package_dir={"": "src"},
    packages=setuptools.find_packages(where="src"),
    python_requires=">=3.6",
)
